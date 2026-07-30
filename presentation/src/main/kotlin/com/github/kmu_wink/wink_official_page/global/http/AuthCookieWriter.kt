package com.github.kmu_wink.wink_official_page.global.http

import com.github.kmu_wink.wink_official_page.global.security.AuthCookieNames
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AuthCookieWriter(
    @Value("\${app.jwt.access-token-expiration-hours}")
    private val accessTokenExpirationHours: Long,
    @Value("\${app.jwt.refresh-token-expiration-hours}")
    private val refreshTokenExpirationHours: Long,
    @Value("\${app.auth.cookie-secure:false}")
    private val secure: Boolean,
) {
    fun write(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        response.addCookie(
            AuthCookieNames.ACCESS_TOKEN,
            accessToken,
            Duration.ofHours(accessTokenExpirationHours),
        )
        response.addCookie(
            AuthCookieNames.REFRESH_TOKEN,
            refreshToken,
            Duration.ofHours(refreshTokenExpirationHours),
        )
    }

    fun clear(response: HttpServletResponse) {
        response.addCookie(AuthCookieNames.ACCESS_TOKEN, "", Duration.ZERO)
        response.addCookie(AuthCookieNames.REFRESH_TOKEN, "", Duration.ZERO)
    }

    private fun HttpServletResponse.addCookie(name: String, value: String, maxAge: Duration) {
        addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString(),
        )
    }
}
