package com.github.kmu_wink.wink_official_page.domain.program.study.dto.response

import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.study.schema.Study

data class GetStudiesResponse(
    val studies: PageResult<Study>,
)
