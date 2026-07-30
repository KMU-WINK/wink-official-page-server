package com.github.kmu_wink.wink_official_page.domain.program.history.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import java.time.LocalDate

class History(
    var title: String = "",
    var image: String? = null,
    var date: LocalDate? = null,
) : BaseSchema()
