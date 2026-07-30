package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.dto.response

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit

data class GetRecruitsResponse(
    var recruits: List<Recruit> = emptyList(),
)
