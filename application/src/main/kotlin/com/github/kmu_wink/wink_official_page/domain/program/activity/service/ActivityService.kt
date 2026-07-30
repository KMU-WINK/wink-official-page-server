package com.github.kmu_wink.wink_official_page.domain.program.activity.service

import com.github.kmu_wink.wink_official_page.application.port.out.repository.ActivityRepository
import com.github.kmu_wink.wink_official_page.domain.program.activity.dto.response.GetActivitiesResponse
import org.springframework.stereotype.Service

@Service
class ActivityService(
    private val activityRepository: ActivityRepository,
) {
    fun getActivities(): GetActivitiesResponse {
        val histories = activityRepository.findAllPinned()
        return GetActivitiesResponse(histories)
    }
}
