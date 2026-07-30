package com.github.kmu_wink.wink_official_page.domain.program.project.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateProjectRequest(
    @field:NotBlank
    val title: String,

    @field:NotBlank
    @field:Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE)
    val image: String,

    @field:NotBlank
    val description: String,

    @field:NotBlank
    @field:Pattern(regexp = RegExp.GITHUB_PROJECT_URL_EXPRESSION, message = RegExp.GITHUB_PROJECT_URL_MESSAGE)
    val link: String,
)
