package com.github.kmu_wink.wink_official_page.domain.user.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema

abstract class BaseUser(
    var email: String? = null,
    var name: String? = null,
    var studentId: String? = null,
    var department: String? = null,
    var phoneNumber: String? = null,
) : BaseSchema()
