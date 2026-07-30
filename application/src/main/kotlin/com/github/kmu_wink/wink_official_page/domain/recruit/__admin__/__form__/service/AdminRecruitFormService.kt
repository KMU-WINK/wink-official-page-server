package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.service

import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitSmsRepository
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.GetFormDetailResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.GetFormsResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.RecruitFormDetailResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.RecruitFormSummaryResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.exception.AdminRecruitFormExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.exception.AdminRecruitSmsExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.exception.RecruitExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.outbox.RecruitSmsOutboxMessage
import com.github.kmu_wink.wink_official_page.domain.recruit.outbox.RecruitSmsOutboxPort
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminRecruitFormService(
    private val recruitRepository: RecruitRepository,
    private val recruitFormRepository: RecruitFormRepository,
    private val recruitSmsRepository: RecruitSmsRepository,
    private val recruitSmsOutboxPort: RecruitSmsOutboxPort,
) {
    fun paperClear(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_RECRUIT_PERIOD.toException() }
        assertStep(recruit, Recruit.Step.PRE, AdminRecruitFormExceptionCode.ALREADY_PAPER_ENDED)

        val recruitForm = findForm(formId, recruit)
        recruitForm.paperPass = null

        recruitFormRepository.save(recruitForm)
    }

    fun paperPass(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PRE, AdminRecruitFormExceptionCode.ALREADY_PAPER_ENDED)

        val recruitForm = findForm(formId, recruit)
        recruitForm.paperPass = true

        recruitFormRepository.save(recruitForm)
    }

    fun paperFail(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PRE, AdminRecruitFormExceptionCode.ALREADY_PAPER_ENDED)

        val recruitForm = findForm(formId, recruit)
        recruitForm.paperPass = false

        recruitFormRepository.save(recruitForm)
    }

    fun finalizePaper(recruitId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PRE, AdminRecruitFormExceptionCode.ALREADY_PAPER_ENDED)

        val recruitEndDate = recruit.recruitEndDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        if (!LocalDateTime.now().isAfter(recruitEndDate.atTime(23, 59, 59))) {
            throw AdminRecruitFormExceptionCode.RECRUITING.toException()
        }

        val forms = recruitFormRepository.findAllByRecruit(recruit)

        val sms = recruitSmsRepository.findByRecruit(recruit)
        val paperFail = sms.paperFail
        val paperPass = sms.paperPass
        if (paperFail == null || paperPass == null) {
            throw AdminRecruitSmsExceptionCode.MESSAGE_IS_EMPTY.toException()
        }

        recruitSmsOutboxPort.enqueue(
            forms.map { form ->
                val pass = form.paperPass == true
                RecruitSmsOutboxMessage(
                    dedupeKey = smsDedupeKey(recruit, form, if (pass) "PAPER_PASS" else "PAPER_FAIL"),
                    recruitId = requireNotNull(recruit.id),
                    requiredStep = Recruit.Step.PAPER_END,
                    target = form.phoneNumber ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException(),
                    content = RecruitSms.transform(if (pass) paperPass else paperFail, form),
                )
            },
        )

        recruit.step = Recruit.Step.PAPER_END
        recruitRepository.save(recruit)
    }

    fun interviewClear(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PAPER_END, AdminRecruitFormExceptionCode.ALREADY_INTERVIEW_ENDED)

        val recruitForm = findInterviewForm(formId, recruit)
        recruitForm.interviewPass = null

        recruitFormRepository.save(recruitForm)
    }

    fun interviewPass(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PAPER_END, AdminRecruitFormExceptionCode.ALREADY_INTERVIEW_ENDED)

        val recruitForm = findInterviewForm(formId, recruit)
        recruitForm.interviewPass = true

        recruitFormRepository.save(recruitForm)
    }

    fun interviewFail(recruitId: String, formId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PAPER_END, AdminRecruitFormExceptionCode.ALREADY_INTERVIEW_ENDED)

        val recruitForm = findInterviewForm(formId, recruit)
        recruitForm.interviewPass = false

        recruitFormRepository.save(recruitForm)
    }

    fun finalizeInterview(recruitId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        assertStep(recruit, Recruit.Step.PAPER_END, AdminRecruitFormExceptionCode.ALREADY_INTERVIEW_ENDED)

        val forms = recruitFormRepository.findAllByRecruit(recruit)
            .filter { it.paperPass == true }

        val sms = recruitSmsRepository.findByRecruit(recruit)
        val finalFail = sms.finalFail
        val finalPass = sms.finalPass
        if (finalFail == null || finalPass == null) {
            throw AdminRecruitSmsExceptionCode.MESSAGE_IS_EMPTY.toException()
        }

        recruitSmsOutboxPort.enqueue(
            forms.map { form ->
                if (form.interviewPass == true) {
                    RecruitSmsOutboxMessage(
                        dedupeKey = smsDedupeKey(recruit, form, "FINAL_PASS"),
                        recruitId = requireNotNull(recruit.id),
                        requiredStep = Recruit.Step.INTERVIEW_END,
                        target = form.phoneNumber ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException(),
                        content = finalPass,
                        recruitFormId = requireNotNull(form.id),
                        createPreUserBeforeSend = true,
                    )
                } else {
                    RecruitSmsOutboxMessage(
                        dedupeKey = smsDedupeKey(recruit, form, "FINAL_FAIL"),
                        recruitId = requireNotNull(recruit.id),
                        requiredStep = Recruit.Step.INTERVIEW_END,
                        target = form.phoneNumber ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException(),
                        content = RecruitSms.transform(finalFail, form),
                    )
                }
            },
        )

        recruit.step = Recruit.Step.INTERVIEW_END
        recruitRepository.save(recruit)
    }

    fun getForms(recruitId: String): GetFormsResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val forms = recruitFormRepository.findAllByRecruitOrderByCreatedAtDesc(recruit)
            .map(RecruitFormSummaryResponse::from)

        return GetFormsResponse(forms)
    }

    fun getForm(recruitId: String, formId: String): GetFormDetailResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val form = findForm(formId, recruit)

        return GetFormDetailResponse(RecruitFormDetailResponse.from(form))
    }

    private fun findForm(formId: String, recruit: Recruit): RecruitForm =
        recruitFormRepository.findByIdAndRecruit(formId, recruit)
            .orElseThrow { AdminRecruitFormExceptionCode.NOT_FOUND.toException() }

    private fun findInterviewForm(formId: String, recruit: Recruit): RecruitForm {
        val recruitForm = findForm(formId, recruit)
        if (recruitForm.paperPass != true) {
            throw AdminRecruitFormExceptionCode.PAPER_FAILED_USER.toException()
        }
        return recruitForm
    }

    private fun assertStep(
        recruit: Recruit,
        expected: Recruit.Step,
        exceptionCode: AdminRecruitFormExceptionCode,
    ) {
        if (recruit.step != expected) {
            throw exceptionCode.toException()
        }
    }

    private fun smsDedupeKey(recruit: Recruit, form: RecruitForm, event: String): String {
        val recruitKey = recruit.id ?: "${recruit.year}-${recruit.semester}"
        val formKey = form.id ?: listOfNotNull(form.studentId, form.email, form.phoneNumber).joinToString(":")
        if (formKey.isBlank()) {
            throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        }

        return "recruit:$recruitKey:form:$formKey:$event"
    }
}
