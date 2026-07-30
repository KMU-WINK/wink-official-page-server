package com.github.kmu_wink.wink_official_page.global.security.authentication

import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserAuthentication(
    private val user: User,
    private val password: String? = null,
) : Authentication {
    private var authenticated = false

    override fun getAuthorities(): Collection<GrantedAuthority> =
        user.role
            ?.authorizationRoles()
            ?.map { SimpleGrantedAuthority("ROLE_${it.name}") }
            .orEmpty()

    override fun getCredentials(): String? = password

    override fun getDetails(): Any? = null

    override fun getPrincipal(): User = user

    override fun isAuthenticated(): Boolean = authenticated

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

    override fun getName(): String = requireNotNull(user.id)
}
