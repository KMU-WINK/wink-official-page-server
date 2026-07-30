package com.github.kmu_wink.wink_official_page.domain.recruit.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class EmailCheckRequest(
    @field:NotBlank
    @field:Pattern(regexp = RegExp.KOOKMIN_EMAIL_EXPRESSION, message = RegExp.KOOKMIN_EMAIL_MESSAGE)
    var email: String = "",
)
