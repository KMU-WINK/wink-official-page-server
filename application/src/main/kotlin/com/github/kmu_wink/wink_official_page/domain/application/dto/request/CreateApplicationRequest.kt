package com.github.kmu_wink.wink_official_page.domain.application.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateApplicationRequest(
    @field:NotBlank
    val name: String,
)
