package com.github.kmu_wink.wink_official_page.domain.user.__admin__.service

import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxBarrier
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxMessage
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxPort
import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.application.port.out.UserDirectoryQueryPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.kmu_wink.wink_official_page.domain.auth.exception.AuthExceptionCode
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.request.InviteRequest
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.request.UpdateRequest
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.response.AdminPreUserResponse
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.response.AdminPreUsersResponse
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.response.AdminUsersResponse
import com.github.kmu_wink.wink_official_page.domain.user.__admin__.util.email.InviteTemplate
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserPayload
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserResponse
import com.github.kmu_wink.wink_official_page.domain.user.exception.UserExceptionCode
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val preUserRepository: PreUserRepository,
    private val userDirectoryQueryPort: UserDirectoryQueryPort,
    private val mailOutboxPort: MailOutboxPort,
) {
    fun getUsers(page: Int, query: String): AdminUsersResponse {
        val pageQuery = PageQuery(
            page,
            20,
            listOf(
                PageQuery.Sort("role", PageQuery.Direction.DESC),
                PageQuery.Sort("fee", PageQuery.Direction.DESC),
                PageQuery.Sort("name"),
            ),
        )
        val users = userDirectoryQueryPort.findAdminUsers(query, pageQuery)

        return AdminUsersResponse(
            PageResult(
                content = users.content.map(UserPayload::from),
                page = users.page,
                size = users.size,
                totalElements = users.totalElements,
            ),
        )
    }

    fun getPreUsers(page: Int, query: String): AdminPreUsersResponse {
        val pageQuery = PageQuery(page, 20, listOf(PageQuery.Sort("name")))
        val users = preUserRepository.findAllSearch(query.toSafeRegex(), pageQuery)

        return AdminPreUsersResponse(users)
    }

    fun invite(dto: InviteRequest): AdminPreUserResponse {
        if (
            userRepository.findByStudentId(dto.studentId).isPresent ||
            userRepository.findByEmail(dto.email).isPresent ||
            userRepository.findByPhoneNumber(dto.phoneNumber).isPresent ||
            preUserRepository.findByStudentId(dto.studentId).isPresent ||
            preUserRepository.findByEmail(dto.email).isPresent ||
            preUserRepository.findByPhoneNumber(dto.phoneNumber).isPresent
        ) {
            throw AuthExceptionCode.ALREADY_REGISTERED.toException()
        }

        var preUser = PreUser(
            name = dto.name,
            studentId = dto.studentId,
            department = dto.department,
            email = dto.email,
            phoneNumber = dto.phoneNumber,
            token = RandomString.generate(128),
            test = false,
        )

        val inviteToken = requireNotNull(preUser.token)
        mailOutboxPort.enqueue(
            MailOutboxMessage.of(
                dedupeScope = "admin-invite",
                recipient = dto.email,
                template = InviteTemplate.of(preUser),
                barrier = MailOutboxBarrier(
                    type = MailOutboxBarrier.Type.PRE_USER_TOKEN,
                    token = inviteToken,
                ),
            ),
        )

        preUser = try {
            preUserRepository.save(preUser)
        } catch (_: DuplicateEntityException) {
            throw AuthExceptionCode.ALREADY_REGISTERED.toException()
        }

        return AdminPreUserResponse(preUser)
    }

    fun removePreUser(id: String) {
        val preUser = preUserRepository.findById(id).orElseThrow(UserExceptionCode.NOT_FOUND::toException)
        preUserRepository.delete(preUser)
    }

    fun update(id: String, dto: UpdateRequest): UserResponse {
        val user = userRepository.findById(id).orElseThrow(UserExceptionCode.NOT_FOUND::toException)

        user.name = dto.name
        user.studentId = dto.studentId
        user.department = dto.department
        user.email = dto.email
        user.phoneNumber = dto.phoneNumber
        user.role = dto.role
        user.fee = dto.fee

        val savedUser = try {
            userRepository.save(user)
        } catch (_: DuplicateEntityException) {
            throw AuthExceptionCode.ALREADY_REGISTERED.toException()
        }

        return UserResponse(savedUser)
    }

    private fun String.toSafeRegex(): String = Pattern.quote(this)
}
