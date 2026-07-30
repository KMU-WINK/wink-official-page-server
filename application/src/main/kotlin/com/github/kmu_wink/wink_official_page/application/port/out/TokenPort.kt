package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.domain.user.schema.User

interface TokenPort {
    fun generateAccessToken(user: User): String

    fun generateAccessToken(userId: String): String

    fun generateRefreshToken(user: User): String

    fun generateRefreshToken(userId: String): String
}
