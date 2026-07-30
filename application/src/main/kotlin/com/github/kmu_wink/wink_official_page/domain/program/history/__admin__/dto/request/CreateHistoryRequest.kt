package com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateHistoryRequest(
    @field:NotBlank
    val title: String,

    @field:NotBlank
    @field:Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE)
    val image: String,

    @field:NotNull
    val date: LocalDate,
)
