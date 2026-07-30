package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import java.util.Optional

interface UserRepository {
    fun findById(id: String): Optional<User>
    fun findByStudentId(studentId: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findByPhoneNumber(phoneNumber: String): Optional<User>
    fun save(user: User): User
}
