package com.github.kmu_wink.wink_official_page.domain.program.study.controller

import com.github.kmu_wink.wink_official_page.domain.program.study.dto.response.GetCategoriesResponse
import com.github.kmu_wink.wink_official_page.domain.program.study.dto.response.GetStudiesResponse
import com.github.kmu_wink.wink_official_page.domain.program.study.service.StudyService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [Study] Index")
@RestController
@RequestMapping("/program/study")
class StudyController(
    private val studyService: StudyService,
) {
    @GetMapping
    @Operation(summary = "모든 게시글 목록")
    fun getStudies(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "") query: String,
    ): ApiResponse<GetStudiesResponse> = ApiResponse.ok(studyService.getStudies(page, query))

    @GetMapping("/{category}")
    @Operation(summary = "카테고리 게시글 목록")
    fun getStudies(
        @PathVariable category: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "") query: String,
    ): ApiResponse<GetStudiesResponse> = ApiResponse.ok(studyService.getStudies(category, page, query))

    @GetMapping("/category")
    @Operation(summary = "모든 카테고리 목록")
    fun getCategories(): ApiResponse<GetCategoriesResponse> = ApiResponse.ok(studyService.getCategories())
}
