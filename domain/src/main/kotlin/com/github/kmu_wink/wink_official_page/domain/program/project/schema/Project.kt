package com.github.kmu_wink.wink_official_page.domain.program.project.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import com.github.kmu_wink.wink_official_page.domain.user.schema.User

class Project(
    var author: User? = null,
    var title: String = "",
    var description: String = "",
    var image: String? = null,
    var link: String = "",
) : BaseSchema()
