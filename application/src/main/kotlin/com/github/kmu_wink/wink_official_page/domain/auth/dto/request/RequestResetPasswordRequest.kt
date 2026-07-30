package com.github.kmu_wink.wink_official_page.domain.auth.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RequestResetPasswordRequest(
    @field:NotBlank
    @field:Pattern(regexp = RegExp.KOOKMIN_EMAIL_EXPRESSION, message = RegExp.KOOKMIN_EMAIL_MESSAGE)
    val email: String,
)
