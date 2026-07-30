package com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.service

import com.github.kmu_wink.wink_official_page.application.port.out.repository.HistoryRepository
import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.dto.request.CreateHistoryRequest
import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.dto.response.GetHistoryResponse
import com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.exception.HistoryAdminExceptionCode
import com.github.kmu_wink.wink_official_page.domain.program.history.schema.History
import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import org.springframework.stereotype.Service

@Service
class AdminHistoryService(
    private val historyRepository: HistoryRepository,
    private val objectStoragePort: ObjectStoragePort,
) {
    fun createHistory(dto: CreateHistoryRequest): GetHistoryResponse {
        var history = History(title = dto.title, image = dto.image, date = dto.date)
        history = historyRepository.save(history)
        return GetHistoryResponse(history)
    }

    fun updateHistory(id: String, dto: CreateHistoryRequest): GetHistoryResponse {
        var history = historyRepository.findById(id).orElseThrow { HistoryAdminExceptionCode.NOT_FOUND.toException() }
        history.title = dto.title
        history.image = dto.image
        history.date = dto.date
        history = historyRepository.save(history)
        return GetHistoryResponse(history)
    }

    fun deleteHistory(id: String) {
        val history = historyRepository.findById(id).orElseThrow { HistoryAdminExceptionCode.NOT_FOUND.toException() }
        history.image?.let { objectStoragePort.deleteUrl(it, StorageNamespace.PROGRAM) }
        historyRepository.delete(history)
    }
}
