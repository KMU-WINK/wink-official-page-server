package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import java.time.LocalDateTime

data class GetFormsResponse(
    val forms: List<RecruitFormSummaryResponse> = emptyList(),
)

data class RecruitFormSummaryResponse(
    val id: String,
    val name: String,
    val studentId: String,
    val department: String,
    val paperPass: Boolean?,
    val interviewPass: Boolean?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(form: RecruitForm): RecruitFormSummaryResponse =
            RecruitFormSummaryResponse(
                id = requireNotNull(form.id),
                name = form.name.orEmpty(),
                studentId = form.studentId.orEmpty(),
                department = form.department.orEmpty(),
                paperPass = form.paperPass,
                interviewPass = form.interviewPass,
                createdAt = form.createdAt,
            )
    }
}
