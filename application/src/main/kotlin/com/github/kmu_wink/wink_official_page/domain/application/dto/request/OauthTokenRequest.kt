package com.github.kmu_wink.wink_official_page.domain.application.dto.request

import jakarta.validation.constraints.NotBlank

data class OauthTokenRequest(
    @field:NotBlank
    val clientId: String,
    @field:NotBlank
    val clientSecret: String,
    @field:NotBlank
    val token: String,
)
