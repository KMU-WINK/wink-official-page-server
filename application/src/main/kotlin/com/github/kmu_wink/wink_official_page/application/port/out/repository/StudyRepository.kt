package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.program.study.schema.Study
import java.util.Optional

interface StudyRepository {
    fun findAll(): List<Study>
    fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Study>
    fun findAllByCategoryAndSearch(category: String, query: String, pageQuery: PageQuery): PageResult<Study>
    fun findTopByOrderByIndexDesc(): Optional<Study>
    fun save(study: Study): Study
}
