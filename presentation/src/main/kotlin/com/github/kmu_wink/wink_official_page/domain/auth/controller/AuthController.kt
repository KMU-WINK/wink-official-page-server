package com.github.kmu_wink.wink_official_page.domain.auth.controller

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
import com.github.kmu_wink.wink_official_page.domain.auth.service.AuthService
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserResponse
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.http.AuthCookieWriter
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.AuthCookieNames
import com.github.kmu_wink.wink_official_page.global.security.guard.IsMember
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "[Auth] Index")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val authCookieWriter: AuthCookieWriter,
) {
    @GetMapping("/csrf")
    @Operation(summary = "CSRF 토큰 발급")
    fun csrf(csrfToken: CsrfToken): ApiResponse<Map<String, String>> =
        ApiResponse.ok(
            mapOf(
                "token" to csrfToken.token,
                "headerName" to csrfToken.headerName,
                "parameterName" to csrfToken.parameterName,
            ),
        )

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(
        @RequestBody @Valid request: LoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<Void> {
        val tokens = authService.login(request)
        authCookieWriter.write(response, tokens.accessToken, tokens.refreshToken)
        return ApiResponse.ok()
    }

    @PostMapping("/register/check")
    @Operation(summary = "회원가입 토큰 확인")
    fun checkRegister(@RequestBody @Valid request: CheckRegisterRequest): ApiResponse<CheckRegisterResponse> =
        ApiResponse.ok(authService.checkRegister(request))

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    fun register(@RequestBody @Valid request: RegisterRequest): ApiResponse<Void> {
        authService.register(request)
        return ApiResponse.ok()
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "토큰 갱신")
    fun refresh(
        servletRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<Void> {
        val refreshToken = servletRequest.cookies
            ?.firstOrNull { it.name == AuthCookieNames.REFRESH_TOKEN }
            ?.value
            ?: throw AuthExceptionCode.INVALID_REFRESH_TOKEN.toException()
        val tokens = authService.refresh(RefreshRequest(refreshToken))
        authCookieWriter.write(response, tokens.accessToken, tokens.refreshToken)
        return ApiResponse.ok()
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    fun logout(servletRequest: HttpServletRequest, response: HttpServletResponse): ApiResponse<Void> {
        val refreshToken = servletRequest.cookies
            ?.firstOrNull { it.name == AuthCookieNames.REFRESH_TOKEN }
            ?.value
        authService.logout(refreshToken)
        authCookieWriter.clear(response)
        return ApiResponse.ok()
    }

    @PostMapping("/reset-password/request")
    @Operation(summary = "비밀번호 초기화 요청")
    fun requestResetPassword(@RequestBody @Valid request: RequestResetPasswordRequest): ApiResponse<Void> {
        authService.requestResetPassword(request)
        return ApiResponse.ok()
    }

    @PostMapping("/reset-password/check")
    @Operation(summary = "비밀번초 초기화 토큰 확인")
    fun checkResetPassword(
        @RequestBody @Valid request: CheckResetPasswordRequest,
    ): ApiResponse<CheckResetPasswordResponse> =
        ApiResponse.ok(authService.checkResetPassword(request))

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 초기화")
    fun resetPassword(@RequestBody @Valid request: ResetPasswordRequest): ApiResponse<Void> {
        authService.resetPassword(request)
        return ApiResponse.ok()
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 확인")
    @IsMember
    fun me(@AuthenticationPrincipal user: User): ApiResponse<UserResponse> =
        ApiResponse.ok(authService.me(user))
}
