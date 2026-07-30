package com.github.kmu_wink.wink_official_page.domain.recruit.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PhoneNumberCheckRequest(
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PHONE_NUMBER_EXPRESSION, message = RegExp.PHONE_NUMBER_MESSAGE)
    var phoneNumber: String = "",
)
