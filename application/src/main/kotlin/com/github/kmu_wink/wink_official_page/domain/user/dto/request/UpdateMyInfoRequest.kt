package com.github.kmu_wink.wink_official_page.domain.user.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.annotation.Nullable
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateMyInfoRequest(
    @field:Nullable
    @field:Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE)
    val avatar: String? = null,
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
