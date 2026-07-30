package com.github.kmu_wink.wink_official_page.domain.program.project.service

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ProjectRepository
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.request.CreateProjectRequest
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectResponse
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectsPageableResponse
import com.github.kmu_wink.wink_official_page.domain.program.project.exception.ProjectExceptionCode
import com.github.kmu_wink.wink_official_page.domain.program.project.schema.Project
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
) {
    fun getProjects(page: Int, query: String): GetProjectsPageableResponse {
        val pageQuery = PageQuery(page, 15, listOf(PageQuery.Sort("createdAt", PageQuery.Direction.DESC)))
        val projects = projectRepository.findAllSearch(query.toSafeRegex(), pageQuery)
        return GetProjectsPageableResponse(projects)
    }

    fun createProject(user: User, dto: CreateProjectRequest): GetProjectResponse {
        var project = Project(
            author = user,
            title = dto.title,
            description = dto.description,
            image = dto.image,
            link = dto.link,
        )
        project = projectRepository.save(project)
        return GetProjectResponse(project)
    }

    fun updateProject(user: User, id: String, dto: CreateProjectRequest): GetProjectResponse {
        var project = projectRepository.findById(id).orElseThrow { ProjectExceptionCode.NOT_FOUND.toException() }
        if (project.author != user) {
            throw ProjectExceptionCode.NOT_OWNER.toException()
        }
        project.title = dto.title
        project.description = dto.description
        project.image = dto.image
        project.link = dto.link
        project = projectRepository.save(project)
        return GetProjectResponse(project)
    }

    fun deleteProject(user: User, id: String) {
        val project = projectRepository.findById(id).orElseThrow { ProjectExceptionCode.NOT_FOUND.toException() }
        if (project.author != user) {
            throw ProjectExceptionCode.NOT_OWNER.toException()
        }
        projectRepository.delete(project)
    }

    private fun String.toSafeRegex(): String = Pattern.quote(this)
}
