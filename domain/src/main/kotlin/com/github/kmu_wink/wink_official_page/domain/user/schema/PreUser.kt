package com.github.kmu_wink.wink_official_page.domain.user.schema

class PreUser(
    email: String? = null,
    name: String? = null,
    studentId: String? = null,
    department: String? = null,
    phoneNumber: String? = null,
    var token: String? = null,
    var test: Boolean = false,
) : BaseUser(
    email = email,
    name = name,
    studentId = studentId,
    department = department,
    phoneNumber = phoneNumber,
)
