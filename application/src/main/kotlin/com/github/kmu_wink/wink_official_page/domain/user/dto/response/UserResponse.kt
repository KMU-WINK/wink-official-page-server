package com.github.kmu_wink.wink_official_page.domain.user.dto.response

import com.github.kmu_wink.wink_official_page.domain.user.schema.User

data class UserResponse(
    val user: UserPayload,
) {
    constructor(user: User) : this(UserPayload.from(user))
}

data class UserPayload(
    val id: String?,
    val email: String?,
    val name: String?,
    val studentId: String?,
    val department: String?,
    val phoneNumber: String?,
    val avatar: String?,
    val description: String?,
    val social: User.Social?,
    val role: User.Role?,
    val fee: Boolean,
) {
    companion object {
        fun from(user: User): UserPayload =
            UserPayload(
                id = user.id,
                email = user.email,
                name = user.name,
                studentId = user.studentId,
                department = user.department,
                phoneNumber = user.phoneNumber,
                avatar = user.avatar,
                description = user.description,
                social = user.social,
                role = user.role,
                fee = user.fee,
            )
    }
}
