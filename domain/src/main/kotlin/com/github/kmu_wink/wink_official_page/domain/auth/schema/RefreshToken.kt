package com.github.kmu_wink.wink_official_page.domain.auth.schema

data class RefreshToken(
    var id: Long? = null,
    var token: String? = null,
    var userId: String? = null,
    var ttl: Long = 0,
)
