package com.github.kmu_wink.wink_official_page.domain.program.activity.controller

import com.github.kmu_wink.wink_official_page.domain.program.activity.dto.response.GetActivitiesResponse
import com.github.kmu_wink.wink_official_page.domain.program.activity.service.ActivityService
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [Activity] Index")
@RestController
@RequestMapping("/program/activity")
class ActivityController(
    private val activityService: ActivityService,
) {
    @GetMapping
    @Operation(summary = "활동 목록")
    fun getActivities(): ApiResponse<GetActivitiesResponse> = ApiResponse.ok(activityService.getActivities())
}
