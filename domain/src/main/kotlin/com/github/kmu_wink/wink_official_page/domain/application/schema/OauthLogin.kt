package com.github.kmu_wink.wink_official_page.domain.application.schema

data class OauthLogin(
    var id: Long? = null,
    var token: String? = null,
    var clientId: String? = null,
    var userId: String? = null,
    var scopes: List<Application.Login.Scope> = emptyList(),
)
