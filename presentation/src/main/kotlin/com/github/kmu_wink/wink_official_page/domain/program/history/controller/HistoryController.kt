package com.github.kmu_wink.wink_official_page.domain.program.history.controller

import com.github.kmu_wink.wink_official_page.domain.program.history.dto.response.GetHistoriesResponse
import com.github.kmu_wink.wink_official_page.domain.program.history.service.HistoryService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [History] Index")
@RestController
@RequestMapping("/program/history")
class HistoryController(
    private val historyService: HistoryService,
) {
    @GetMapping
    @Operation(summary = "연혁 목록")
    fun getHistories(): ApiResponse<GetHistoriesResponse> = ApiResponse.ok(historyService.getHistories())
}
