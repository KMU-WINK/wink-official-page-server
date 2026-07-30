package com.github.kmu_wink.wink_official_page.domain.application.dto.response

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application

data class OauthTokenResponse(
    val user: Map<String, Any?>,
    val scopes: List<Application.Login.Scope>,
)
