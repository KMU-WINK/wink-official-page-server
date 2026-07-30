package com.github.kmu_wink.wink_official_page.domain.user.schema

class User(
    email: String? = null,
    name: String? = null,
    studentId: String? = null,
    department: String? = null,
    phoneNumber: String? = null,
    var password: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var social: Social? = null,
    var role: Role? = null,
    var fee: Boolean = false,
) : BaseUser(
    email = email,
    name = name,
    studentId = studentId,
    department = department,
    phoneNumber = phoneNumber,
) {
    enum class Role(
        private vararg val inheritedRoles: Role,
    ) {
        MEMBER,
        GRADUATED(MEMBER),
        ADMIN(MEMBER),
        PLANNING_ASSISTANT(ADMIN, MEMBER),
        PLANNING_HEAD(PLANNING_ASSISTANT),
        PUBLIC_RELATIONS_ASSISTANT(ADMIN, MEMBER),
        PUBLIC_RELATIONS_HEAD(PUBLIC_RELATIONS_ASSISTANT),
        TECH_ASSISTANT(ADMIN, MEMBER),
        TECH_HEAD(TECH_ASSISTANT),
        TREASURY_ASSISTANT(ADMIN, MEMBER),
        TREASURY_HEAD(TREASURY_ASSISTANT),
        VICE_PRESIDENT(PLANNING_HEAD, PUBLIC_RELATIONS_HEAD, TREASURY_HEAD),
        PRESIDENT(VICE_PRESIDENT),
        ;

        fun authorizationRoles(): Set<Role> {
            val roles = linkedSetOf<Role>()
            collectAuthorization(this, roles)
            return roles
        }

        private fun collectAuthorization(role: Role, roles: MutableSet<Role>) {
            roles.add(role)
            role.inheritedRoles.forEach { collectAuthorization(it, roles) }
        }

        fun toKorean(): String = when (this) {
            PRESIDENT -> "회장"
            VICE_PRESIDENT -> "부회장"
            TREASURY_HEAD -> "총무부 부장"
            TREASURY_ASSISTANT -> "총무부 차장"
            TECH_HEAD -> "학술부 부장"
            TECH_ASSISTANT -> "학술부 차장"
            PUBLIC_RELATIONS_HEAD -> "홍보부 부장"
            PUBLIC_RELATIONS_ASSISTANT -> "홍보부 차장"
            PLANNING_HEAD -> "기획부 부장"
            PLANNING_ASSISTANT -> "기획부 차장"
            GRADUATED -> "졸업생"
            MEMBER -> "부원"
            ADMIN -> throw IllegalStateException("Unexpected value: $this")
        }

        companion object {
            fun fromKorean(role: String): Role = when (role) {
                "회장" -> PRESIDENT
                "부회장" -> VICE_PRESIDENT
                "총무부 부장" -> TREASURY_HEAD
                "총무부 차장" -> TREASURY_ASSISTANT
                "학술부 부장" -> TECH_HEAD
                "학술부 차장" -> TECH_ASSISTANT
                "홍보부 부장" -> PUBLIC_RELATIONS_HEAD
                "홍보부 차장" -> PUBLIC_RELATIONS_ASSISTANT
                "기획부 부장" -> PLANNING_HEAD
                "기획부 차장" -> PLANNING_ASSISTANT
                "졸업생" -> GRADUATED
                "부원" -> MEMBER
                else -> throw IllegalStateException("Unexpected value: $role")
            }
        }
    }

    data class Social(
        var github: String? = null,
        var instagram: String? = null,
        var blog: String? = null,
    )
}
