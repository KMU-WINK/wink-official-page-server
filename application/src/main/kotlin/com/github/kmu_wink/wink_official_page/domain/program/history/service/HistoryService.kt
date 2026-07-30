package com.github.kmu_wink.wink_official_page.domain.program.history.service

import com.github.kmu_wink.wink_official_page.application.port.out.repository.HistoryRepository
import com.github.kmu_wink.wink_official_page.domain.program.history.dto.response.GetHistoriesResponse
import org.springframework.stereotype.Service

@Service
class HistoryService(
    private val historyRepository: HistoryRepository,
) {
    fun getHistories(): GetHistoriesResponse {
        val histories = historyRepository.findAllWithSort()
        return GetHistoriesResponse(histories)
    }
}
