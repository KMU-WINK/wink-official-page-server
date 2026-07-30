package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.program.history.schema.History
import java.util.Optional

interface HistoryRepository {
    fun findAllWithSort(): List<History>
    fun findById(id: String): Optional<History>
    fun save(history: History): History
    fun delete(history: History)
}
