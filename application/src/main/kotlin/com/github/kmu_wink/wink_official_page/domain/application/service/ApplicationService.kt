package com.github.kmu_wink.wink_official_page.domain.application.service

import com.github.kmu_wink.wink_official_page.application.port.out.IdGeneratorPort
import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.OneTimeTokenConsumePort
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ApplicationRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.OauthLoginRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.atomfrede.jadenticon.Jadenticon
import com.github.kmu_wink.wink_official_page.domain.application.dto.request.CreateApplicationRequest
import com.github.kmu_wink.wink_official_page.domain.application.dto.request.OauthTokenRequest
import com.github.kmu_wink.wink_official_page.domain.application.dto.request.UpdateApplicationLoginRequest
import com.github.kmu_wink.wink_official_page.domain.application.dto.request.UpdateApplicationRequest
import com.github.kmu_wink.wink_official_page.domain.application.dto.response.GetApplicationResponse
import com.github.kmu_wink.wink_official_page.domain.application.dto.response.GetApplicationsResponse
import com.github.kmu_wink.wink_official_page.domain.application.dto.response.OauthLoginResponse
import com.github.kmu_wink.wink_official_page.domain.application.dto.response.OauthTokenResponse
import com.github.kmu_wink.wink_official_page.domain.application.exception.ApplicationExceptionCode
import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.domain.application.schema.OauthLogin
import com.github.kmu_wink.wink_official_page.domain.program.upload.dto.response.UploadImageResponse
import com.github.kmu_wink.wink_official_page.domain.user.exception.UserExceptionCode
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val oauthLoginRepository: OauthLoginRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val userRepository: UserRepository,
    private val oneTimeTokenConsumePort: OneTimeTokenConsumePort,
    private val idGeneratorPort: IdGeneratorPort,
) {
    fun getApplications(user: User): GetApplicationsResponse =
        GetApplicationsResponse(applicationRepository.findAllByUser(user).map { com.github.kmu_wink.wink_official_page.domain.application.dto.response.ApplicationPayload.from(it) })

    fun getApplication(user: User, id: String): GetApplicationResponse {
        val application = applicationRepository.findById(id)
            .orElseThrow(ApplicationExceptionCode.NOT_FOUND::toException)

        if (application.user != user) {
            application.secret = null
            application.user = null
        }

        return GetApplicationResponse(application)
    }

    fun createApplication(user: User, dto: CreateApplicationRequest): GetApplicationResponse {
        val id = idGeneratorPort.generateId()
        val now = LocalDateTime.now()
        val application = Application(
            baseId = id,
            createdAt = now,
            updatedAt = now,
            name = dto.name,
            img = objectStoragePort.upload("application/$id.png", Jadenticon.from(id).png()),
            secret = RandomString.generate(96),
            user = user,
            login = Application.Login(),
        )

        return GetApplicationResponse(applicationRepository.save(application))
    }

    fun uploadImg(user: User): UploadImageResponse =
        UploadImageResponse(
            "application/${requireNotNull(user.id)}/${UUID.randomUUID()}".let(objectStoragePort::generatePresignedUrl),
        )

    fun updateApplication(user: User, id: String, dto: UpdateApplicationRequest): GetApplicationResponse {
        val application = ownedApplication(user, id)
        if (
            dto.img != application.img &&
            !objectStoragePort.isOwnedUrl(
                dto.img,
                StorageNamespace.APPLICATION,
                requireNotNull(user.id),
            )
        ) {
            throw ApplicationExceptionCode.INVALID_IMAGE_URL.toException()
        }

        if (application.img != dto.img) {
            application.img?.let { objectStoragePort.deleteUrl(it, StorageNamespace.APPLICATION) }
        }

        application.name = dto.name
        application.img = dto.img

        return GetApplicationResponse(applicationRepository.save(application))
    }

    fun resetSecret(user: User, id: String): GetApplicationResponse {
        val application = ownedApplication(user, id)
        application.secret = RandomString.generate(96)
        return GetApplicationResponse(applicationRepository.save(application))
    }

    fun updateApplicationLogin(user: User, id: String, dto: UpdateApplicationLoginRequest): GetApplicationResponse {
        val application = ownedApplication(user, id)
        application.login.enable = dto.enable
        application.login.urls = dto.urls
        application.login.scopes = (listOf(Application.Login.Scope.UUID) + dto.scopes)
            .distinct()
            .sortedBy { it.ordinal }

        return GetApplicationResponse(applicationRepository.save(application))
    }

    fun deleteApplication(user: User, id: String) {
        val application = ownedApplication(user, id)
        application.img?.let { objectStoragePort.deleteUrl(it, StorageNamespace.APPLICATION) }
        applicationRepository.delete(application)
    }

    fun oauthLogin(user: User, id: String): OauthLoginResponse {
        val application = applicationRepository.findById(id)
            .orElseThrow(ApplicationExceptionCode.NOT_FOUND::toException)

        if (!application.login.enable) {
            throw ApplicationExceptionCode.OAUTH_NOT_SUPPORTED.toException()
        }

        val oauthLogin = OauthLogin(
            token = RandomString.generate(128),
            clientId = application.id,
            userId = user.id,
            scopes = application.login.scopes,
        )

        oauthLoginRepository.save(oauthLogin)

        return OauthLoginResponse(oauthLogin.token)
    }

    fun oauthToken(dto: OauthTokenRequest): OauthTokenResponse {
        val application = applicationRepository.findById(dto.clientId)
            .orElseThrow(ApplicationExceptionCode.NOT_FOUND::toException)

        if (!secretsMatch(application.secret, dto.clientSecret)) {
            throw ApplicationExceptionCode.INVALID_SECRET.toException()
        }

        val oauthLogin = oneTimeTokenConsumePort.consumeOauthLogin(dto.token)
            ?: throw ApplicationExceptionCode.OAUTH_TOKEN_NOT_FOUND.toException()

        if (oauthLogin.clientId != dto.clientId) {
            throw ApplicationExceptionCode.OAUTH_TOKEN_NOT_FOUND.toException()
        }

        val userId = oauthLogin.userId ?: throw UserExceptionCode.NOT_FOUND.toException()
        val raw = userRepository.findById(userId)
            .orElseThrow(UserExceptionCode.NOT_FOUND::toException)

        val exposedUser = linkedMapOf<String, Any?>()
        val scopes = oauthLogin.scopes

        if (Application.Login.Scope.UUID in scopes) exposedUser["id"] = raw.id
        if (Application.Login.Scope.EMAIL in scopes) exposedUser["email"] = raw.email
        if (Application.Login.Scope.NAME in scopes) exposedUser["name"] = raw.name
        if (Application.Login.Scope.STUDENT_ID in scopes) exposedUser["studentId"] = raw.studentId
        if (Application.Login.Scope.DEPARTMENT in scopes) exposedUser["department"] = raw.department
        if (Application.Login.Scope.PHONE_NUMBER in scopes) exposedUser["phoneNumber"] = raw.phoneNumber
        if (Application.Login.Scope.AVATAR in scopes) exposedUser["avatar"] = raw.avatar
        if (Application.Login.Scope.DESCRIPTION in scopes) exposedUser["description"] = raw.description
        if (Application.Login.Scope.SOCIAL in scopes) exposedUser["social"] = raw.social
        if (Application.Login.Scope.ROLE in scopes) exposedUser["role"] = raw.role
        if (Application.Login.Scope.FEE in scopes) exposedUser["fee"] = raw.fee

        return OauthTokenResponse(user = exposedUser, scopes = scopes)
    }

    private fun ownedApplication(user: User, id: String): Application {
        val application = applicationRepository.findById(id)
            .orElseThrow(ApplicationExceptionCode.NOT_FOUND::toException)

        if (application.user != user) {
            throw ApplicationExceptionCode.NOT_FOUND.toException()
        }

        return application
    }

    private fun secretsMatch(expected: String?, actual: String): Boolean {
        if (expected == null) {
            return false
        }

        return MessageDigest.isEqual(
            expected.toByteArray(StandardCharsets.UTF_8),
            actual.toByteArray(StandardCharsets.UTF_8),
        )
    }
}
