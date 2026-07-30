package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import java.util.Optional

interface PasswordResetTokenRepository {
    fun findByToken(token: String): Optional<PasswordResetToken>
    fun save(passwordResetToken: PasswordResetToken): PasswordResetToken
    fun delete(passwordResetToken: PasswordResetToken)
}
