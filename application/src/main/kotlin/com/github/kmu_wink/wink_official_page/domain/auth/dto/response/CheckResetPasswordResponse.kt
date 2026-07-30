package com.github.kmu_wink.wink_official_page.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class CheckResetPasswordResponse(
    @get:JsonProperty("isValid")
    val isValid: Boolean,
)
