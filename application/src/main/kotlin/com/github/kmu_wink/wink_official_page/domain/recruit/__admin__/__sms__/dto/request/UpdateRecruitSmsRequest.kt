package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.dto.request

import jakarta.validation.constraints.NotBlank

data class UpdateRecruitSmsRequest(
    @field:NotBlank
    var paperFail: String = "",
    @field:NotBlank
    var paperPass: String = "",
    @field:NotBlank
    var finalFail: String = "",
    @field:NotBlank
    var finalPass: String = "",
)
