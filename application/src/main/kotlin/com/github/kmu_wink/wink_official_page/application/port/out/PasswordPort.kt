package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.domain.user.schema.User

interface PasswordPort {
    fun encode(rawPassword: String): String

    fun verify(user: User, rawPassword: String)
}
