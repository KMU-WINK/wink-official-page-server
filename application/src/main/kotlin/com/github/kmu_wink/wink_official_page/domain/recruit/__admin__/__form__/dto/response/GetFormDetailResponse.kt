package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response

import com.github.kmu_wink.wink_official_page.domain.recruit.constant.BackendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DesignTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DevOpsTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.FrontendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import java.time.LocalDate
import java.time.LocalDateTime

data class GetFormDetailResponse(
    val form: RecruitFormDetailResponse,
)

data class RecruitFormDetailResponse(
    val id: String,
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
    val paperPass: Boolean?,
    val interviewPass: Boolean?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(form: RecruitForm): RecruitFormDetailResponse =
            RecruitFormDetailResponse(
                id = requireNotNull(form.id),
                name = form.name.orEmpty(),
                studentId = form.studentId.orEmpty(),
                department = form.department.orEmpty(),
                email = form.email.orEmpty(),
                phoneNumber = form.phoneNumber.orEmpty(),
                jiwonDonggi = form.jiwonDonggi.orEmpty(),
                selfIntroduce = form.selfIntroduce.orEmpty(),
                outings = form.outings.orEmpty(),
                interviewDates = form.interviewDates.orEmpty(),
                whyCannotInterview =
                    if (LocalDate.of(1, 1, 1) in form.interviewDates.orEmpty()) {
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
                paperPass = form.paperPass,
                interviewPass = form.interviewPass,
                createdAt = form.createdAt,
            )
    }
}
