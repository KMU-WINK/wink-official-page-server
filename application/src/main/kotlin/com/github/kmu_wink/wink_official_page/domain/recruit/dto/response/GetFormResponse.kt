package com.github.kmu_wink.wink_official_page.domain.recruit.dto.response

import com.github.kmu_wink.wink_official_page.domain.recruit.constant.BackendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DesignTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DevOpsTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.FrontendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import java.time.LocalDate

data class GetFormResponse(
    val form: RecruitFormResponse,
) {
    companion object {
        fun of(form: RecruitForm): GetFormResponse = GetFormResponse(RecruitFormResponse.of(form))
    }
}

data class RecruitFormResponse(
    val recruit: RecruitEditResponse,
    val name: String,
    val studentId: String,
    val department: String,
    val email: String,
    val phoneNumber: String,
    val jiwonDonggi: String,
    val selfIntroduce: String,
    val outings: List<String>,
    val interviewDates: List<LocalDate>,
    val whyCannotInterview: String?,
    val github: String?,
    val frontendTechStacks: List<FrontendTechStack>,
    val backendTechStacks: List<BackendTechStack>,
    val devOpsTechStacks: List<DevOpsTechStack>,
    val designTechStacks: List<DesignTechStack>,
    val favoriteProject: String?,
    val privacyConsent: Boolean,
    val privacyPolicyVersion: String = RecruitPrivacyPolicy.CURRENT_VERSION,
) {
    companion object {
        fun of(form: RecruitForm): RecruitFormResponse {
            val recruit = requireNotNull(form.recruit) { "Recruit form has no recruit" }
            return RecruitFormResponse(
                recruit = RecruitEditResponse.of(recruit),
                name = form.name.orEmpty(),
                studentId = form.studentId.orEmpty(),
                department = form.department.orEmpty(),
                email = form.email.orEmpty(),
                phoneNumber = form.phoneNumber.orEmpty(),
                jiwonDonggi = form.jiwonDonggi.orEmpty(),
                selfIntroduce = form.selfIntroduce.orEmpty(),
                outings = form.outings.orEmpty(),
                interviewDates = form.interviewDates.orEmpty(),
                whyCannotInterview = if (LocalDate.of(1, 1, 1) in form.interviewDates.orEmpty()) {
                    RecruitPrivacyPolicy.INTERVIEW_SCHEDULE_COORDINATION_REQUIRED
                } else {
                    null
                },
                github = form.github,
                frontendTechStacks = form.frontendTechStacks.orEmpty(),
                backendTechStacks = form.backendTechStacks.orEmpty(),
                devOpsTechStacks = form.devOpsTechStacks.orEmpty(),
                designTechStacks = form.designTechStacks.orEmpty(),
                favoriteProject = form.favoriteProject,
                privacyConsent = form.privacyPolicyVersion == RecruitPrivacyPolicy.CURRENT_VERSION &&
                    form.privacyConsentAcceptedAt != null,
            )
        }
    }
}

data class RecruitEditResponse(
    val id: String?,
    val year: Int,
    val semester: Int,
    val recruitStartDate: LocalDate?,
    val recruitEndDate: LocalDate?,
    val interviewStartDate: LocalDate?,
    val interviewEndDate: LocalDate?,
    val step: Recruit.Step?,
) {
    companion object {
        fun of(recruit: Recruit): RecruitEditResponse =
            RecruitEditResponse(
                id = recruit.id,
                year = recruit.year,
                semester = recruit.semester,
                recruitStartDate = recruit.recruitStartDate,
                recruitEndDate = recruit.recruitEndDate,
                interviewStartDate = recruit.interviewStartDate,
                interviewEndDate = recruit.interviewEndDate,
                step = recruit.step,
            )
    }
}
