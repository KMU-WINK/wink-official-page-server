package com.github.kmu_wink.wink_official_page.domain.auth.service

import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxBarrier
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxMessage
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxPort
import com.github.kmu_wink.wink_official_page.application.port.out.OneTimeTokenConsumePort
import com.github.kmu_wink.wink_official_page.application.port.out.PasswordPort
import com.github.kmu_wink.wink_official_page.application.port.out.TokenPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PasswordResetTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RefreshTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.CheckRegisterRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.CheckResetPasswordRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.LoginRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.RefreshRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.RegisterRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.RequestResetPasswordRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.request.ResetPasswordRequest
import com.github.kmu_wink.wink_official_page.domain.auth.dto.response.CheckRegisterResponse
import com.github.kmu_wink.wink_official_page.domain.auth.dto.response.CheckResetPasswordResponse
import com.github.kmu_wink.wink_official_page.domain.auth.exception.AuthExceptionCode
import com.github.kmu_wink.wink_official_page.domain.auth.model.AuthTokens
import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import com.github.kmu_wink.wink_official_page.domain.auth.util.email.PasswordResetTokenTemplate
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserResponse
import com.github.kmu_wink.wink_official_page.domain.user.exception.UserExceptionCode
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val preUserRepository: PreUserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val oneTimeTokenConsumePort: OneTimeTokenConsumePort,
    private val passwordPort: PasswordPort,
    private val tokenPort: TokenPort,
    private val mailOutboxPort: MailOutboxPort,
) {
    fun login(dto: LoginRequest): AuthTokens {
        val user = userRepository.findByEmail(dto.email)
            .orElseThrow(AuthExceptionCode.AUTHENTICATION_FAILED::toException)

        passwordPort.verify(user, dto.password)

        val accessToken = tokenPort.generateAccessToken(user)
        val refreshToken = tokenPort.generateRefreshToken(user)

        return AuthTokens(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun checkRegister(dto: CheckRegisterRequest): CheckRegisterResponse {
        val preUser = preUserRepository.findByToken(dto.token)
        return CheckRegisterResponse(isValid = preUser.isPresent, user = preUser.orElse(null))
    }

    fun register(dto: RegisterRequest) {
        val preUser = preUserRepository.findByToken(dto.token)
            .orElseThrow(AuthExceptionCode.INVALID_REGISTER_TOKEN::toException)

        if (preUser.test) {
            throw AuthExceptionCode.TEST_USER_CANNOT_REAL_REGISTER.toException()
        }

        if (
            userRepository.findByEmail(requireNotNull(preUser.email)).isPresent ||
            userRepository.findByStudentId(requireNotNull(preUser.studentId)).isPresent ||
            userRepository.findByPhoneNumber(requireNotNull(preUser.phoneNumber)).isPresent
        ) {
            throw AuthExceptionCode.ALREADY_REGISTERED.toException()
        }

        val user = User(
            email = preUser.email,
            name = preUser.name,
            studentId = preUser.studentId,
            department = preUser.department,
            phoneNumber = preUser.phoneNumber,
            password = passwordPort.encode(dto.password),
            social = User.Social(),
            role = User.Role.MEMBER,
            fee = false,
        )

        try {
            userRepository.save(user)
        } catch (_: DuplicateEntityException) {
            throw AuthExceptionCode.ALREADY_REGISTERED.toException()
        }
        preUserRepository.delete(preUser)
    }

    fun refresh(dto: RefreshRequest): AuthTokens {
        val refreshToken = oneTimeTokenConsumePort.consumeRefreshToken(dto.token)
            ?: throw AuthExceptionCode.INVALID_REFRESH_TOKEN.toException()

        val userId = requireNotNull(refreshToken.userId)
        val accessToken = tokenPort.generateAccessToken(userId)
        val newRefreshToken = tokenPort.generateRefreshToken(userId)

        return AuthTokens(accessToken = accessToken, refreshToken = newRefreshToken)
    }

    fun logout(refreshToken: String?) {
        if (!refreshToken.isNullOrBlank()) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete)
        }
    }

    fun requestResetPassword(dto: RequestResetPasswordRequest) {
        userRepository.findByEmail(dto.email).ifPresent { user ->
            val passwordResetTokenRaw = RandomString.generate(128)
            val passwordResetToken = PasswordResetToken(
                token = passwordResetTokenRaw,
                userId = user.id,
            )

            mailOutboxPort.enqueue(
                MailOutboxMessage.of(
                    dedupeScope = "password-reset",
                    recipient = dto.email,
                    template = PasswordResetTokenTemplate.of(user, passwordResetToken),
                    barrier = MailOutboxBarrier(
                        type = MailOutboxBarrier.Type.PASSWORD_RESET_TOKEN,
                        token = passwordResetTokenRaw,
                    ),
                ),
            )
            passwordResetTokenRepository.save(passwordResetToken)
        }
    }

    fun checkResetPassword(dto: CheckResetPasswordRequest): CheckResetPasswordResponse {
        val isVerified = passwordResetTokenRepository.findByToken(dto.token).isPresent
        return CheckResetPasswordResponse(isValid = isVerified)
    }

    fun resetPassword(request: ResetPasswordRequest) {
        val passwordResetTokenEntity = oneTimeTokenConsumePort.consumePasswordResetToken(request.token)
            ?: throw AuthExceptionCode.INVALID_PASSWORD_RESET_TOKEN.toException()

        val userId = requireNotNull(passwordResetTokenEntity.userId)
        val user = userRepository.findById(userId).orElseThrow(UserExceptionCode.NOT_FOUND::toException)

        user.password = passwordPort.encode(request.newPassword)
        userRepository.save(user)
    }

    fun me(user: User): UserResponse = UserResponse(user)
}
