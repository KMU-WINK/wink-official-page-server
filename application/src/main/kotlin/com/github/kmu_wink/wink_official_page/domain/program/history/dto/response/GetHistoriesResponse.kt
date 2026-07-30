package com.github.kmu_wink.wink_official_page.domain.program.history.dto.response

import com.github.kmu_wink.wink_official_page.domain.program.history.schema.History

data class GetHistoriesResponse(
    val histories: List<History>,
)
