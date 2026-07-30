package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.domain.application.schema.OauthLogin
import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import com.github.kmu_wink.wink_official_page.domain.auth.schema.RefreshToken

interface OneTimeTokenConsumePort {
    fun consumeRefreshToken(token: String): RefreshToken?
    fun consumePasswordResetToken(token: String): PasswordResetToken?
    fun consumeOauthLogin(token: String): OauthLogin?
}
