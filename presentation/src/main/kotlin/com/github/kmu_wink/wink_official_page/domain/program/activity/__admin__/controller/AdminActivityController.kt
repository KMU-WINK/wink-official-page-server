package com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.controller

import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.request.CreateActivityRequest
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.response.GetActivitiesPageableResponse
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.response.GetActivityResponse
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.service.AdminActivityService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@IsAdmin
@Tag(name = "[Program] [Activity] Admin")
@RestController
@RequestMapping("/admin/program/activity")
class AdminActivityController(
    private val adminActivityService: AdminActivityService,
) {
    @GetMapping
    @Operation(summary = "활동 목록")
    fun getActivities(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "") query: String,
    ): ApiResponse<GetActivitiesPageableResponse> = ApiResponse.ok(adminActivityService.getActivities(page, query))

    @PostMapping
    @Operation(summary = "활동 생성")
    fun createActivity(@RequestBody @Valid request: CreateActivityRequest): ApiResponse<GetActivityResponse> =
        ApiResponse.ok(adminActivityService.createActivity(request))

    @PutMapping("/{id}")
    @Operation(summary = "활동 수정")
    fun updateActivity(
        @PathVariable id: String,
        @RequestBody @Valid request: CreateActivityRequest,
    ): ApiResponse<GetActivityResponse> = ApiResponse.ok(adminActivityService.updateActivity(id, request))

    @DeleteMapping("/{id}")
    @Operation(summary = "활동 삭제")
    fun deleteActivity(@PathVariable id: String): ApiResponse<Void> {
        adminActivityService.deleteActivity(id)
        return ApiResponse.ok()
    }

    @PatchMapping("/{id}/pin")
    @Operation(summary = "활동 고정")
    fun pinActivity(@PathVariable id: String): ApiResponse<GetActivityResponse> =
        ApiResponse.ok(adminActivityService.pinActivity(id))

    @DeleteMapping("/{id}/pin")
    @Operation(summary = "활동 고정 해제")
    fun unpinActivity(@PathVariable id: String): ApiResponse<GetActivityResponse> =
        ApiResponse.ok(adminActivityService.unpinActivity(id))
}
