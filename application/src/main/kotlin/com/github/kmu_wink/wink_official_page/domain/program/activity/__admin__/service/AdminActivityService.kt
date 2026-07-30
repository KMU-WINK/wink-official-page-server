package com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.service

import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ActivityRepository
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.request.CreateActivityRequest
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.response.GetActivitiesPageableResponse
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.dto.response.GetActivityResponse
import com.github.kmu_wink.wink_official_page.domain.program.activity.__admin__.exception.ActivityAdminExceptionCode
import com.github.kmu_wink.wink_official_page.domain.program.activity.schema.Activity
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class AdminActivityService(
    private val activityRepository: ActivityRepository,
    private val objectStoragePort: ObjectStoragePort,
) {
    fun getActivities(page: Int, query: String): GetActivitiesPageableResponse {
        val pageQuery = PageQuery(
            page,
            20,
            listOf(
                PageQuery.Sort("pinned", PageQuery.Direction.DESC),
                PageQuery.Sort("createdAt", PageQuery.Direction.DESC),
            ),
        )
        val activities = activityRepository.findAllSearch(query.toSafeRegex(), pageQuery)
        return GetActivitiesPageableResponse(activities)
    }

    fun createActivity(dto: CreateActivityRequest): GetActivityResponse {
        var activity = Activity(
            title = dto.title,
            description = dto.description,
            images = dto.images,
            pinned = false,
        )
        activity = activityRepository.save(activity)
        return GetActivityResponse(activity)
    }

    fun updateActivity(id: String, dto: CreateActivityRequest): GetActivityResponse {
        var activity = activityRepository.findById(id).orElseThrow { ActivityAdminExceptionCode.NOT_FOUND.toException() }
        activity.title = dto.title
        activity.description = dto.description
        activity.images = dto.images
        activity = activityRepository.save(activity)
        return GetActivityResponse(activity)
    }

    fun deleteActivity(id: String) {
        val activity = activityRepository.findById(id).orElseThrow { ActivityAdminExceptionCode.NOT_FOUND.toException() }
        activity.images.forEach { objectStoragePort.deleteUrl(it, StorageNamespace.PROGRAM) }
        activityRepository.delete(activity)
    }

    fun pinActivity(id: String): GetActivityResponse {
        var activity = activityRepository.findById(id).orElseThrow { ActivityAdminExceptionCode.NOT_FOUND.toException() }
        activity.pinned = true
        activity = activityRepository.save(activity)
        return GetActivityResponse(activity)
    }

    fun unpinActivity(id: String): GetActivityResponse {
        var activity = activityRepository.findById(id).orElseThrow { ActivityAdminExceptionCode.NOT_FOUND.toException() }
        activity.pinned = false
        activity = activityRepository.save(activity)
        return GetActivityResponse(activity)
    }

    private fun String.toSafeRegex(): String = Pattern.quote(this)
}
