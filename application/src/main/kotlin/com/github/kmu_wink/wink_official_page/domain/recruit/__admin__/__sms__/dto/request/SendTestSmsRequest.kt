package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.dto.request

import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.constant.TestSmsField
import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class SendTestSmsRequest(
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PHONE_NUMBER_EXPRESSION, message = RegExp.PHONE_NUMBER_MESSAGE)
    var phoneNumber: String = "",
    @field:NotNull
    var field: TestSmsField? = null,
)
