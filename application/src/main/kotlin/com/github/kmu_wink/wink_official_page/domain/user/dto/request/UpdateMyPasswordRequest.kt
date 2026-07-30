package com.github.kmu_wink.wink_official_page.domain.user.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UpdateMyPasswordRequest(
    @field:NotBlank
    val password: String,
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PASSWORD_EXPRESSION, message = RegExp.PASSWORD_MESSAGE)
    val newPassword: String,
)
