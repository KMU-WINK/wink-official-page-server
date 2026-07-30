package com.github.kmu_wink.wink_official_page.domain.application.dto.response

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserPayload

data class GetApplicationResponse(
    val application: ApplicationPayload,
) {
    constructor(application: Application) : this(ApplicationPayload.from(application))
}

data class ApplicationPayload(
    val id: String?,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?,
    val name: String?,
    val img: String?,
    val secret: String?,
    val user: UserPayload?,
    val login: Application.Login,
) {
    companion object {
        fun from(application: Application): ApplicationPayload =
            ApplicationPayload(
                id = application.id,
                createdAt = application.createdAt,
                updatedAt = application.updatedAt,
                name = application.name,
                img = application.img,
                secret = application.secret,
                user = application.user?.let(UserPayload::from),
                login = application.login,
            )
    }
}
