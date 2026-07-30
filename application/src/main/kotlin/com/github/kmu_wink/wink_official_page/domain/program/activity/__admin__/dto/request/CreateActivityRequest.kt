package com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateActivityRequest(
    @field:NotBlank
    val title: String,

    @field:NotBlank
    val description: String,

    @field:NotNull
    val images: List<@Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE) String>,
)
