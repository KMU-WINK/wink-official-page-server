package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.project.schema.Project
import java.util.Optional

interface ProjectRepository {
    fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Project>
    fun findById(id: String): Optional<Project>
    fun save(project: Project): Project
    fun delete(project: Project)
}
