package com.github.kmu_wink.wink_official_page.domain.program.project.__admin__.controller

import com.github.kmu_wink.wink_official_page.domain.program.project.__admin__.service.AdminProjectService
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.request.CreateProjectRequest
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectResponse
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsAdmin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [Project] Admin")
@IsAdmin
@RestController
@RequestMapping("/admin/program/project")
class AdminProjectController(
    private val adminProjectService: AdminProjectService,
) {
    @PutMapping("/{id}")
    @Operation(summary = "프로젝트 수정")
    fun updateProject(
        @PathVariable id: String,
        @RequestBody @Valid request: CreateProjectRequest,
    ): ApiResponse<GetProjectResponse> = ApiResponse.ok(adminProjectService.updateProject(id, request))

    @DeleteMapping("/{id}")
    @Operation(summary = "프로젝트 삭제")
    fun deleteProject(@PathVariable id: String): ApiResponse<Void> {
        adminProjectService.deleteProject(id)
        return ApiResponse.ok()
    }
}
