package com.github.kmu_wink.wink_official_page.domain.auth.schema

data class PasswordResetToken(
    var id: Long? = null,
    var token: String? = null,
    var userId: String? = null,
)
