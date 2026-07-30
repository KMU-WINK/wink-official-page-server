package com.github.kmu_wink.wink_official_page.domain.auth.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    val token: String,
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PASSWORD_EXPRESSION, message = RegExp.PASSWORD_MESSAGE)
    val password: String,
    @field:Nullable
    @field:Size(max = 30)
    val description: String? = null,
    @field:Nullable
    @field:Pattern(regexp = RegExp.GITHUB_USERNAME_EXPRESSION, message = RegExp.GITHUB_USERNAME_MESSAGE)
    val github: String? = null,
    @field:Nullable
    @field:Pattern(regexp = RegExp.INSTAGRAM_EXPRESSION, message = RegExp.INSTAGRAM_MESSAGE)
    val instagram: String? = null,
    @field:Nullable
    @field:Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE)
    val blog: String? = null,
)
