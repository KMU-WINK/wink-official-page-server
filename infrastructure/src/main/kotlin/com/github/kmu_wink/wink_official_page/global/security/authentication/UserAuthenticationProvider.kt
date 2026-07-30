package com.github.kmu_wink.wink_official_page.global.security.authentication

import com.github.kmu_wink.wink_official_page.domain.auth.exception.AuthExceptionCode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class UserAuthenticationProvider : AuthenticationProvider {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    override fun authenticate(authentication: Authentication): Authentication {
        val userAuthentication = authentication as UserAuthentication
        val credentials = userAuthentication.credentials
        val principal = userAuthentication.principal
        val password = principal.password

        if (credentials.isNullOrBlank() || password.isNullOrBlank() || !passwordEncoder().matches(credentials, password)) {
            throw AuthExceptionCode.AUTHENTICATION_FAILED.toException()
        }

        userAuthentication.isAuthenticated = true
        return userAuthentication
    }

    override fun supports(authentication: Class<*>): Boolean = authentication == UserAuthentication::class.java
}
