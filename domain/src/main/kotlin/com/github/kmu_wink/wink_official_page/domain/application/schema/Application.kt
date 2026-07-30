package com.github.kmu_wink.wink_official_page.domain.application.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import java.time.LocalDateTime

class Application(
    baseId: String? = null,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    var name: String? = null,
    var img: String? = null,
    var secret: String? = null,
    var user: User? = null,
    var login: Login = Login(),
) : BaseSchema() {
    init {
        restoreIdentity(baseId, createdAt, updatedAt)
    }

    data class Login(
        var enable: Boolean = false,
        var urls: List<String> = emptyList(),
        var scopes: List<Scope> = listOf(Scope.UUID),
    ) {
        enum class Scope {
            UUID,
            EMAIL,
            NAME,
            STUDENT_ID,
            DEPARTMENT,
            PHONE_NUMBER,
            AVATAR,
            DESCRIPTION,
            SOCIAL,
            ROLE,
            FEE,
        }
    }
}
