package com.github.kmu_wink.wink_official_page.domain.user.adapter

import com.github.kmu_wink.wink_official_page.application.port.out.PasswordPort
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.security.authentication.UserAuthentication
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class SpringSecurityPasswordAdapter(
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) : PasswordPort {
    override fun encode(rawPassword: String): String =
        requireNotNull(passwordEncoder.encode(rawPassword)) { "Password encoder returned null" }

    override fun verify(user: User, rawPassword: String) {
        authenticationManager.authenticate(UserAuthentication(user, rawPassword))
    }
}
