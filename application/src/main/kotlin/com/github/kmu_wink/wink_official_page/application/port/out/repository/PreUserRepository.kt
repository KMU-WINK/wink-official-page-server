package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import java.util.Optional

interface PreUserRepository {
    fun findById(id: String): Optional<PreUser>
    fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<PreUser>
    fun findByToken(token: String): Optional<PreUser>
    fun findByTestTrueAndStudentId(studentId: String): Optional<PreUser>
    fun findByStudentId(studentId: String): Optional<PreUser>
    fun findByEmail(email: String): Optional<PreUser>
    fun findByPhoneNumber(phoneNumber: String): Optional<PreUser>
    fun save(preUser: PreUser): PreUser
    fun delete(preUser: PreUser)
}
