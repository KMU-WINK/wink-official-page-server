package com.github.kmu_wink.wink_official_page.domain.program.study.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import java.time.LocalDateTime

class Study(
    var index: Int = 0,
    var category: String = "",
    var title: String = "",
    var author: String = "",
    var content: String = "",
    var image: String? = null,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
) : BaseSchema() {
    init {
        restoreIdentity(id, createdAt, updatedAt)
    }
}
