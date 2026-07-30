package com.github.kmu_wink.wink_official_page.domain.recruit.outbox

import com.github.kmu_wink.wink_official_page.application.port.out.SmsGatewayPort
import com.github.kmu_wink.wink_official_page.application.port.out.SmsMessage
import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(
    prefix = "app.recruit.sms-outbox",
    name = ["worker-enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class RecruitSmsOutboxWorker(
    private val recruitSmsOutboxRepository: RecruitSmsOutboxRepository,
    private val recruitRepository: RecruitRepository,
    private val recruitFormRepository: RecruitFormRepository,
    private val preUserRepository: PreUserRepository,
    private val smsGatewayPort: SmsGatewayPort,
    @Value("\${app.recruit.sms-outbox.batch-size:20}")
    private val batchSize: Int,
    @Value("\${app.recruit.sms-outbox.max-attempts:5}")
    private val maxAttempts: Int,
    @Value("\${app.recruit.sms-outbox.base-backoff-seconds:30}")
    private val baseBackoffSeconds: Long,
    @Value("\${app.recruit.sms-outbox.max-state-waits:120}")
    private val maxStateWaits: Int,
    @Value("\${app.recruit.sms-outbox.lock-timeout-seconds:300}")
    lockTimeoutSeconds: Long,
) {
    private val lockTimeout: Duration = Duration.ofSeconds(lockTimeoutSeconds)

    @Scheduled(fixedDelayString = "\${app.recruit.sms-outbox.fixed-delay-ms:5000}")
    fun drain() {
        repeat(batchSize.coerceAtLeast(1)) {
            val claimed = recruitSmsOutboxRepository.claim(lockTimeout) ?: return
            deliver(claimed)
        }
    }

    private fun deliver(claimed: ClaimedRecruitSmsOutbox) {
        val outbox = claimed.outbox
        val id = outbox.id ?: return

        val recruit = recruitRepository.findById(outbox.recruitId).orElse(null)
        if (recruit == null) {
            recruitSmsOutboxRepository.markCanceled(
                id,
                claimed.lockToken,
                "Recruit ${outbox.recruitId} no longer exists",
            )
            return
        }

        if (!hasReached(recruit.step, outbox.requiredStep)) {
            if (outbox.stateWaits >= maxStateWaits.coerceAtLeast(1)) {
                recruitSmsOutboxRepository.markCanceled(
                    id,
                    claimed.lockToken,
                    "Recruit ${outbox.recruitId} did not reach ${outbox.requiredStep}",
                )
                return
            }
            recruitSmsOutboxRepository.deferUntilStateCommitted(
                id,
                claimed.lockToken,
                LocalDateTime.now().plusSeconds(5),
            )
            return
        }

        try {
            val content = contentAfterStateBarrier(outbox)
            smsGatewayPort.send(listOf(SmsMessage(outbox.target, content)))
            recruitSmsOutboxRepository.markProcessed(id, claimed.lockToken)
        } catch (exception: Exception) {
            recruitSmsOutboxRepository.markFailed(
                id = id,
                lockToken = claimed.lockToken,
                attempts = outbox.attempts,
                maxAttempts = maxAttempts.coerceAtLeast(1),
                nextAttemptAt = LocalDateTime.now().plusSeconds(backoffSeconds(outbox.attempts)),
                error = exception.javaClass.name,
            )
        }
    }

    private fun backoffSeconds(attempts: Int): Long {
        val exponent = attempts.coerceIn(1, 6) - 1
        return baseBackoffSeconds.coerceAtLeast(1) shl exponent
    }

    private fun hasReached(current: Recruit.Step?, required: Recruit.Step): Boolean =
        current != null && current.ordinal >= required.ordinal

    private fun contentAfterStateBarrier(outbox: RecruitSmsOutbox): String {
        if (!outbox.createPreUserBeforeSend) {
            return outbox.content
        }

        val formId = outbox.recruitFormId ?: return outbox.content
        val form = recruitFormRepository.findById(formId).orElse(null) ?: return outbox.content
        return RecruitSms.transform(outbox.content, ensurePreUser(form))
    }

    private fun ensurePreUser(form: RecruitForm): PreUser {
        val studentId = form.studentId ?: return createPreUser(form)
        return preUserRepository.findByStudentId(studentId).orElseGet {
            try {
                createPreUser(form)
            } catch (_: DuplicateEntityException) {
                preUserRepository.findByStudentId(studentId).orElseThrow()
            }
        }
    }

    private fun createPreUser(form: RecruitForm): PreUser =
        preUserRepository.save(
            PreUser(
                email = form.email,
                name = form.name,
                studentId = form.studentId,
                department = form.department,
                phoneNumber = form.phoneNumber,
                token = RandomString.generate(128),
                test = false,
            ),
        )
}
