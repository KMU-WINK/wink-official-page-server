package com.github.kmu_wink.wink_official_page.domain.program.project.dto.response

import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.project.schema.Project

data class GetProjectsPageableResponse(
    val projects: PageResult<Project>,
)
