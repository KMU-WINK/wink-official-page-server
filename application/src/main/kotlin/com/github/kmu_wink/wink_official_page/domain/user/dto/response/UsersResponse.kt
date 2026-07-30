package com.github.kmu_wink.wink_official_page.domain.user.dto.response

data class UsersResponse(
    val users: List<PublicUserResponse>,
)

data class PublicUserResponse(
    val id: String?,
    val name: String?,
    val avatar: String?,
    val description: String?,
    val social: PublicSocialResponse?,
    val role: String?,
)

data class PublicSocialResponse(
    val github: String?,
    val instagram: String?,
    val blog: String?,
)
