package com.github.kmu_wink.wink_official_page.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank
    val token: String,
)
