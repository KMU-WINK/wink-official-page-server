package com.github.kmu_wink.wink_official_page.domain.program.project.controller

import com.github.kmu_wink.wink_official_page.domain.program.project.dto.request.CreateProjectRequest
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectResponse
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectsPageableResponse
import com.github.kmu_wink.wink_official_page.domain.program.project.service.ProjectService
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.guard.IsMember
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Program] [Project] Index")
@RestController
@RequestMapping("/program/project")
class ProjectController(
    private val projectService: ProjectService,
) {
    @GetMapping
    @Operation(summary = "프로젝트 목록")
    fun getProjects(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "") query: String,
    ): ApiResponse<GetProjectsPageableResponse> = ApiResponse.ok(projectService.getProjects(page, query))

    @PostMapping
    @IsMember
    @Operation(summary = "프로젝트 생성")
    fun createProject(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: CreateProjectRequest,
    ): ApiResponse<GetProjectResponse> = ApiResponse.ok(projectService.createProject(user, request))

    @PutMapping("/{id}")
    @IsMember
    @Operation(summary = "프로젝트 수정")
    fun updateProject(
        @AuthenticationPrincipal user: User,
        @PathVariable id: String,
        @RequestBody @Valid request: CreateProjectRequest,
    ): ApiResponse<GetProjectResponse> = ApiResponse.ok(projectService.updateProject(user, id, request))

    @DeleteMapping("/{id}")
    @IsMember
    @Operation(summary = "프로젝트 삭제")
    fun deleteProject(@AuthenticationPrincipal user: User, @PathVariable id: String): ApiResponse<Void> {
        projectService.deleteProject(user, id)
        return ApiResponse.ok()
    }
}
