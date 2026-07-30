package com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.controller

import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.dto.request.CreateHistoryRequest
import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.dto.response.GetHistoryResponse
import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.service.AdminHistoryService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [History] Admin")
@IsAdmin
@RestController
@RequestMapping("/admin/program/history")
class AdminHistoryController(
    private val adminHistoryService: AdminHistoryService,
) {
    @PostMapping
    @Operation(summary = "연혁 생성")
    fun getHistories(@RequestBody @Valid request: CreateHistoryRequest): ApiResponse<GetHistoryResponse> =
        ApiResponse.ok(adminHistoryService.createHistory(request))

    @PutMapping("/{id}")
    @Operation(summary = "연혁 수정")
    fun updateHistory(
        @PathVariable id: String,
        @RequestBody @Valid request: CreateHistoryRequest,
    ): ApiResponse<GetHistoryResponse> = ApiResponse.ok(adminHistoryService.updateHistory(id, request))

    @DeleteMapping("/{id}")
    @Operation(summary = "연혁 삭제")
    fun deleteHistory(@PathVariable id: String): ApiResponse<Void> {
        adminHistoryService.deleteHistory(id)
        return ApiResponse.ok()
    }
}
