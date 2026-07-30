package com.github.kmu_wink.wink_official_page.domain.recruit.dto.response

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit

data class GetRecruitResponse(
    var recruit: Recruit? = null,
)
