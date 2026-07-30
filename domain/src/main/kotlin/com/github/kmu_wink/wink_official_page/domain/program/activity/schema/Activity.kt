package com.github.kmu_wink.wink_official_page.domain.program.activity.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema

class Activity(
    var title: String = "",
    var description: String = "",
    var images: List<String> = emptyList(),
    var pinned: Boolean = false,
) : BaseSchema()
