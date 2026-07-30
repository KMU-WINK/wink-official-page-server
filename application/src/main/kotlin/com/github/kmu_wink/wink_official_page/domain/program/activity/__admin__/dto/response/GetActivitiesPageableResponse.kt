package com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.response

import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.activity.schema.Activity

data class GetActivitiesPageableResponse(
    val activities: PageResult<Activity>,
)
