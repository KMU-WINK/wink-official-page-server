package com.github.kmu_wink.wink_official_page.domain.user.service

import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.PasswordPort
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import com.github.kmu_wink.wink_official_page.application.port.out.UserDirectoryQueryPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.kmu_wink.wink_official_page.domain.program.upload.dto.response.UploadImageResponse
import com.github.kmu_wink.wink_official_page.domain.user.dto.request.UpdateMyInfoRequest
import com.github.kmu_wink.wink_official_page.domain.user.dto.request.UpdateMyPasswordRequest
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserResponse
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UsersResponse
import com.github.kmu_wink.wink_official_page.domain.user.exception.UserExceptionCode
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userDirectoryQueryPort: UserDirectoryQueryPort,
    private val passwordPort: PasswordPort,
    private val objectStoragePort: ObjectStoragePort,
) {
    fun getUsers(): UsersResponse = UsersResponse(userDirectoryQueryPort.findPublicMembers())

    fun updateMyInfo(user: User, dto: UpdateMyInfoRequest): UserResponse {
        val userId = requireNotNull(user.id)
        if (
            dto.avatar != null &&
            dto.avatar != user.avatar &&
            !objectStoragePort.isOwnedUrl(dto.avatar, StorageNamespace.AVATAR, userId)
        ) {
            throw UserExceptionCode.INVALID_AVATAR_URL.toException()
        }

        user.avatar = dto.avatar
        user.description = dto.description
        val social = user.social ?: User.Social().also { user.social = it }
        social.github = dto.github
        social.instagram = dto.instagram
        social.blog = dto.blog

        return UserResponse(userRepository.save(user))
    }

    fun uploadMyAvatar(user: User): UploadImageResponse {
        val url = objectStoragePort.generatePresignedUrl("avatar/${requireNotNull(user.id)}/${UUID.randomUUID()}")
        return UploadImageResponse(url)
    }

    fun deleteMyAvatar(user: User): UserResponse {
        if (user.avatar == null) {
            return UserResponse(user)
        }

        user.avatar?.let { objectStoragePort.deleteUrl(it, StorageNamespace.AVATAR) }
        user.avatar = null

        return UserResponse(userRepository.save(user))
    }

    fun updateMyPassword(user: User, dto: UpdateMyPasswordRequest) {
        passwordPort.verify(user, dto.password)

        user.password = passwordPort.encode(dto.newPassword)
        userRepository.save(user)
    }
}
