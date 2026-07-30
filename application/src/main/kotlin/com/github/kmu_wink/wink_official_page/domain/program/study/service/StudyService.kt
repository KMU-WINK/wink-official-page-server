package com.github.kmu_wink.wink_official_page.domain.program.study.service

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.repository.StudyRepository
import com.github.kmu_wink.wink_official_page.domain.program.study.dto.response.GetCategoriesResponse
import com.github.kmu_wink.wink_official_page.domain.program.study.dto.response.GetStudiesResponse
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class StudyService(
    private val studyRepository: StudyRepository,
) {
    fun getStudies(page: Int, query: String): GetStudiesResponse {
        val pageQuery = PageQuery(page, 20, listOf(PageQuery.Sort("index", PageQuery.Direction.DESC)))
        val studies = studyRepository.findAllSearch(query.toSafeRegex(), pageQuery)
        return GetStudiesResponse(studies)
    }

    fun getStudies(category: String, page: Int, query: String): GetStudiesResponse {
        val pageQuery = PageQuery(page, 20, listOf(PageQuery.Sort("index", PageQuery.Direction.DESC)))
        val studies = studyRepository.findAllByCategoryAndSearch(category, query.toSafeRegex(), pageQuery)
        return GetStudiesResponse(studies)
    }

    fun getCategories(): GetCategoriesResponse {
        val categories = studyRepository.findAll().map { it.category }.distinct()
        return GetCategoriesResponse(categories)
    }

    private fun String.toSafeRegex(): String = Pattern.quote(this)
}
