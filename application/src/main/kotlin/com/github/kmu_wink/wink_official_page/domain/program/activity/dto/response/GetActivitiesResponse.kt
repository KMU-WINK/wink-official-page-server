package com.github.kmu_wink.wink_official_page.domain.program.activity.dto.response

import com.github.kmu_wink.wink_official_page.domain.program.activity.schema.Activity

data class GetActivitiesResponse(
    val activities: List<Activity>,
)
