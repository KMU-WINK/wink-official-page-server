package com.github.kmu_wink.wink_official_page.domain.recruit.controller

import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.EmailCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.PhoneNumberCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.RecruitEditSessionFormRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.RecruitEditSessionRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.RecruitFormRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.StudentIdCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.DuplicationCheckResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.GetFormResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.GetRecruitResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.service.RecruitService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Recruit] Index")
@RestController
@RequestMapping("/recruit")
class RecruitController(
    private val recruitService: RecruitService,
) {
    @GetMapping("/latest")
    @Operation(summary = "최근 모집")
    fun getLatestRecruit(): ApiResponse<GetRecruitResponse> =
        ApiResponse.ok(recruitService.getLatestRecruit())

    @PostMapping("/{recruitId}")
    @Operation(summary = "지원서 제출하기")
    fun submitForm(
        @PathVariable recruitId: String,
        @RequestBody @Valid request: RecruitFormRequest,
    ): ApiResponse<Void> {
        recruitService.saveForm(recruitId, request)

        return ApiResponse.ok()
    }

    @PostMapping("/edit-session")
    @Operation(summary = "지원서 수정 세션 조회")
    fun getEditForm(
        @RequestBody @Valid request: RecruitEditSessionRequest,
    ): ResponseEntity<ApiResponse<GetFormResponse>> =
        ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(ApiResponse.ok(recruitService.getEditForm(request.editToken)))

    @PostMapping("/edit-session/form")
    @Operation(summary = "지원서 수정하기")
    fun editForm(
        @RequestBody @Valid request: RecruitEditSessionFormRequest,
    ): ApiResponse<Void> {
        recruitService.editForm(request.editToken, request.form)

        return ApiResponse.ok()
    }

    @PostMapping("/{recruitId}/check/studentId")
    @Operation(summary = "학번 중복 확인")
    fun checkStudentId(
        @PathVariable recruitId: String,
        @RequestBody @Valid request: StudentIdCheckRequest,
    ): ApiResponse<DuplicationCheckResponse> =
        ApiResponse.ok(recruitService.checkStudentId(recruitId, request))

    @PostMapping("/{recruitId}/check/email")
    @Operation(summary = "이메일 중복 확인")
    fun checkEmail(
        @PathVariable recruitId: String,
        @RequestBody @Valid request: EmailCheckRequest,
    ): ApiResponse<DuplicationCheckResponse> =
        ApiResponse.ok(recruitService.checkEmail(recruitId, request))

    @PostMapping("/{recruitId}/check/phoneNumber")
    @Operation(summary = "전화번호 중복 확인")
    fun checkPhoneNumber(
        @PathVariable recruitId: String,
        @RequestBody @Valid request: PhoneNumberCheckRequest,
    ): ApiResponse<DuplicationCheckResponse> =
        ApiResponse.ok(recruitService.checkPhoneNumber(recruitId, request))
}
