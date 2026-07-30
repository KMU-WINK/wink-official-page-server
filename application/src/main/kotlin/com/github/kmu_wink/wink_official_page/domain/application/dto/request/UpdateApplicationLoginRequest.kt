package com.github.kmu_wink.wink_official_page.domain.application.dto.request

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class UpdateApplicationLoginRequest(
    val enable: Boolean,
    @field:NotNull
    val urls: List<@Pattern(regexp = RegExp.URL_EXPRESSION, message = RegExp.URL_MESSAGE) String>,
    @field:NotNull
    val scopes: List<Application.Login.Scope>,
)
