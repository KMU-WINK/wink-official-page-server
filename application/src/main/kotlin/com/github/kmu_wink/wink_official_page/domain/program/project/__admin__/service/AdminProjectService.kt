package com.github.kmu_wink.wink_official_page.domain.program.project.__admin__.service

import com.github.kmu_wink.wink_official_page.application.port.out.repository.ProjectRepository
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.request.CreateProjectRequest
import com.github.kmu_wink.wink_official_page.domain.program.project.dto.response.GetProjectResponse
import com.github.kmu_wink.wink_official_page.domain.program.project.exception.ProjectExceptionCode
import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import org.springframework.stereotype.Service

@Service
class AdminProjectService(
    private val projectRepository: ProjectRepository,
    private val objectStoragePort: ObjectStoragePort,
) {
    fun updateProject(id: String, dto: CreateProjectRequest): GetProjectResponse {
        var project = projectRepository.findById(id).orElseThrow { ProjectExceptionCode.NOT_FOUND.toException() }
        project.title = dto.title
        project.description = dto.description
        project.image = dto.image
        project.link = dto.link
        project = projectRepository.save(project)
        return GetProjectResponse(project)
    }

    fun deleteProject(id: String) {
        val project = projectRepository.findById(id).orElseThrow { ProjectExceptionCode.NOT_FOUND.toException() }
        project.image?.let { objectStoragePort.deleteUrl(it, StorageNamespace.PROGRAM) }
        projectRepository.delete(project)
    }
}
