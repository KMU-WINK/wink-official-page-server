package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.controller

import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.GetFormDetailResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.dto.response.GetFormsResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.service.AdminRecruitFormService
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Recruit] [Form] Admin")
@IsAdmin
@RestController
@RequestMapping("/admin/recruit/{recruitId}/form")
class AdminRecruitFormController(
    private val adminRecruitFormService: AdminRecruitFormService,
) {
    @GetMapping
    @Operation(summary = "신청자 목록")
    fun getForms(
        @AuthenticationPrincipal user: User,
        @PathVariable recruitId: String,
    ): ApiResponse<GetFormsResponse> {
        privacyAudit.info(
            "privacy_access action=recruit_form_list actorId={} recruitId={}",
            user.id,
            recruitId,
        )
        return ApiResponse.ok(adminRecruitFormService.getForms(recruitId))
    }

    @GetMapping("/{formId}")
    @Operation(summary = "신청자 상세")
    fun getForm(
        @AuthenticationPrincipal user: User,
        @PathVariable recruitId: String,
        @PathVariable formId: String,
    ): ApiResponse<GetFormDetailResponse> {
        privacyAudit.info(
            "privacy_access action=recruit_form_detail actorId={} recruitId={} formId={}",
            user.id,
            recruitId,
            formId,
        )
        return ApiResponse.ok(adminRecruitFormService.getForm(recruitId, formId))
    }

    @PostMapping("/{formId}/paper/clear")
    @Operation(summary = "서류 합격 초기화")
    fun paperClear(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.paperClear(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/{formId}/paper/pass")
    @Operation(summary = "서류 합격 처리")
    fun paperPass(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.paperPass(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/{formId}/paper/fail")
    @Operation(summary = "서류 불합격 처리")
    fun paperFail(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.paperFail(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/paper/finalize")
    @Operation(summary = "서류 결과 확정")
    fun finalizePaper(@PathVariable recruitId: String): ApiResponse<Void> {
        adminRecruitFormService.finalizePaper(recruitId)

        return ApiResponse.ok()
    }

    @PostMapping("/{formId}/interview/clear")
    @Operation(summary = "면접 합격 초기화")
    fun interviewClear(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.interviewClear(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/{formId}/interview/pass")
    @Operation(summary = "면접 합격 처리")
    fun interviewPass(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.interviewPass(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/{formId}/interview/fail")
    @Operation(summary = "면접 불합격 처리")
    fun interviewFail(@PathVariable recruitId: String, @PathVariable formId: String): ApiResponse<Void> {
        adminRecruitFormService.interviewFail(recruitId, formId)

        return ApiResponse.ok()
    }

    @PostMapping("/interview/finalize")
    @Operation(summary = "면접 결과 확정")
    fun finalizeInterview(@PathVariable recruitId: String): ApiResponse<Void> {
        adminRecruitFormService.finalizeInterview(recruitId)

        return ApiResponse.ok()
    }

    companion object {
        private val privacyAudit = LoggerFactory.getLogger("privacy.audit")
    }
}
