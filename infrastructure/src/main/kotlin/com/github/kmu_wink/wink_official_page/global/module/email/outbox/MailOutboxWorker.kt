package com.github.kmu_wink.wink_official_page.global.module.email.outbox

import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxBarrier
import com.github.kmu_wink.wink_official_page.application.port.out.MailPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PasswordResetTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography
import com.github.kmu_wink.wink_official_page.global.module.email.EmailTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(
    prefix = "app.mail.outbox",
    name = ["worker-enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class MailOutboxWorker(
    private val mailOutboxRepository: MailOutboxRepository,
    private val preUserRepository: PreUserRepository,
    private val recruitFormRepository: RecruitFormRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val mailPort: MailPort,
    private val recruitPiiCryptography: RecruitPiiCryptography,
    @Value("\${app.mail.outbox.batch-size:20}")
    private val batchSize: Int,
    @Value("\${app.mail.outbox.max-attempts:5}")
    private val maxAttempts: Int,
    @Value("\${app.mail.outbox.base-backoff-seconds:30}")
    private val baseBackoffSeconds: Long,
    @Value("\${app.mail.outbox.max-state-waits:120}")
    private val maxStateWaits: Int,
    @Value("\${app.mail.outbox.lock-timeout-seconds:300}")
    lockTimeoutSeconds: Long,
) {
    private val lockTimeout: Duration = Duration.ofSeconds(lockTimeoutSeconds)

    @Scheduled(fixedDelayString = "\${app.mail.outbox.fixed-delay-ms:5000}")
    fun drain() {
        repeat(batchSize.coerceAtLeast(1)) {
            val claimed = mailOutboxRepository.claim(lockTimeout) ?: return
            deliver(claimed)
        }
    }

    private fun deliver(claimed: ClaimedMailOutbox) {
        val outbox = claimed.outbox
        val id = outbox.id ?: return

        if (!barrierCommitted(outbox)) {
            if (outbox.stateWaits >= maxStateWaits.coerceAtLeast(1)) {
                mailOutboxRepository.markCanceled(
                    id,
                    claimed.lockToken,
                    "Mail barrier ${outbox.barrierType} was not committed",
                )
                return
            }
            mailOutboxRepository.deferUntilBarrierCommitted(
                id,
                claimed.lockToken,
                LocalDateTime.now().plusSeconds(5),
            )
            return
        }

        try {
            mailPort.send(outbox.recipient, StoredEmailTemplate(outbox.title, outbox.html))
            mailOutboxRepository.markProcessed(id, claimed.lockToken)
        } catch (exception: Exception) {
            mailOutboxRepository.markFailed(
                id = id,
                lockToken = claimed.lockToken,
                attempts = outbox.attempts,
                maxAttempts = maxAttempts.coerceAtLeast(1),
                nextAttemptAt = LocalDateTime.now().plusSeconds(backoffSeconds(outbox.attempts)),
                error = exception.javaClass.name,
            )
        }
    }

    private fun barrierCommitted(outbox: MailOutbox): Boolean =
        when (outbox.barrierType) {
            MailOutboxBarrier.Type.PRE_USER_TOKEN ->
                preUserRepository.findByToken(outbox.barrierToken).isPresent
            MailOutboxBarrier.Type.RECRUIT_FORM_EDIT_TOKEN ->
                recruitFormRepository.findByEditTokenDigest(outbox.barrierToken).isPresent ||
                    recruitFormRepository.findByEditTokenDigest(
                        recruitPiiCryptography.editTokenDigest(outbox.barrierToken),
                    ).isPresent ||
                    recruitFormRepository.findByLegacyEditToken(outbox.barrierToken).isPresent
            MailOutboxBarrier.Type.PASSWORD_RESET_TOKEN ->
                passwordResetTokenRepository.findByToken(outbox.barrierToken).isPresent
        }

    private fun backoffSeconds(attempts: Int): Long {
        val exponent = attempts.coerceIn(1, 6) - 1
        return baseBackoffSeconds.coerceAtLeast(1) shl exponent
    }

    private data class StoredEmailTemplate(
        private val title: String,
        private val html: String,
    ) : EmailTemplate {
        override fun getTitle(): String = title

        override fun getHtml(): String = html
    }
}
