package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.activity.schema.Activity
import java.util.Optional

interface ActivityRepository {
    fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Activity>
    fun findAllPinned(): List<Activity>
    fun findById(id: String): Optional<Activity>
    fun save(activity: Activity): Activity
    fun delete(activity: Activity)
}
