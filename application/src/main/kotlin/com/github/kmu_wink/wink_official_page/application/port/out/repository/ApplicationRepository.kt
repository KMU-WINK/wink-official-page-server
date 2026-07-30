package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import java.util.Optional

interface ApplicationRepository {
    fun findAllByUser(user: User): List<Application>
    fun findById(id: String): Optional<Application>
    fun save(application: Application): Application
    fun delete(application: Application)
}
