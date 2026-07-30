package com.github.kmu_wink.wink_official_page.domain.recruit.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.BackendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DesignTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DevOpsTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.FrontendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.util.validation.DepartmentValidate
import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class RecruitFormRequest(
    @field:NotBlank
    @field:Pattern(regexp = RegExp.NAME_EXPRESSION, message = RegExp.NAME_MESSAGE)
    @field:Size(max = 20)
    var name: String = "",
    @field:NotBlank
    @field:Size(min = 8, max = 8, message = RegExp.STUDENT_ID_MESSAGE)
    var studentId: String = "",
    @field:NotBlank
    @field:DepartmentValidate
    @field:Size(max = 100)
    var department: String = "",
    @field:NotBlank
    @field:Pattern(regexp = RegExp.KOOKMIN_EMAIL_EXPRESSION, message = RegExp.KOOKMIN_EMAIL_MESSAGE)
    @field:Size(max = 254)
    var email: String = "",
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PHONE_NUMBER_EXPRESSION, message = RegExp.PHONE_NUMBER_MESSAGE)
    @field:Size(max = 20)
    var phoneNumber: String = "",
    @field:NotBlank
    @field:Size(min = 300, max = 500)
    var jiwonDonggi: String = "",
    @field:NotBlank
    @field:Size(min = 300, max = 500)
    var selfIntroduce: String = "",
    @field:NotNull
    @field:Size(max = 20)
    var outings: List<@NotBlank @Size(max = 100) String> = emptyList(),
    @field:NotNull
    @field:Size(min = 1, max = 31)
    var interviewDates: List<LocalDate> = emptyList(),
    @field:Size(max = 200)
    var whyCannotInterview: String? = null,
    @field:Pattern(regexp = RegExp.GITHUB_USERNAME_EXPRESSION, message = RegExp.GITHUB_USERNAME_MESSAGE)
    @field:Size(max = 39)
    var github: String? = null,
    @field:NotNull
    @field:Size(max = 50)
    var frontendTechStacks: List<FrontendTechStack> = emptyList(),
    @field:NotNull
    @field:Size(max = 50)
    var backendTechStacks: List<BackendTechStack> = emptyList(),
    @field:NotNull
    @field:Size(max = 50)
    var devOpsTechStacks: List<DevOpsTechStack> = emptyList(),
    @field:NotNull
    @field:Size(max = 50)
    var designTechStacks: List<DesignTechStack> = emptyList(),
    @field:Size(min = 100, max = 1000)
    var favoriteProject: String? = null,
    @field:AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
    var privacyConsent: Boolean = false,
    @field:Pattern(
        regexp = RecruitPrivacyPolicy.CURRENT_VERSION,
        message = "현재 개인정보 처리방침에 동의해야 합니다.",
    )
    var privacyPolicyVersion: String = "",
) {
    override fun toString(): String = "RecruitFormRequest([REDACTED])"
}

class RecruitEditSessionRequest(
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @field:NotBlank
    @field:Size(min = 64, max = 256)
    val editToken: String = "",
) {
    override fun toString(): String = "RecruitEditSessionRequest(editToken=[REDACTED])"
}

class RecruitEditSessionFormRequest(
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @field:NotBlank
    @field:Size(min = 64, max = 256)
    val editToken: String = "",
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @field:NotNull
    @field:Valid
    val form: RecruitFormRequest = RecruitFormRequest(),
) {
    override fun toString(): String =
        "RecruitEditSessionFormRequest(editToken=[REDACTED], form=[REDACTED])"
}
