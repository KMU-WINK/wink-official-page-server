package com.github.kmu_wink.wink_official_page.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser

data class CheckRegisterResponse(
    @get:JsonProperty("isValid")
    val isValid: Boolean,
    val user: PreUser?,
)
