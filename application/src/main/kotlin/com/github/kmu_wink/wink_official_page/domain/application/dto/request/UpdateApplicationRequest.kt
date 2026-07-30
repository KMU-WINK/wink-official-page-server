package com.github.kmu_wink.wink_official_page.domain.application.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UpdateApplicationRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @field:Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE)
    val img: String,
)
