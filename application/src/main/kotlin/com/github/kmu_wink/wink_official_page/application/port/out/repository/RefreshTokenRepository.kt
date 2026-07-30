package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.auth.schema.RefreshToken
import java.util.Optional

interface RefreshTokenRepository {
    fun findByToken(token: String): Optional<RefreshToken>
    fun save(refreshToken: RefreshToken): RefreshToken
    fun delete(refreshToken: RefreshToken)
}
