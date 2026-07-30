package com.github.kmu_wink.wink_official_page.global.security.jwt

import com.auth0.jwt.exceptions.TokenExpiredException
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.kmu_wink.wink_official_page.domain.auth.exception.AuthExceptionCode
import com.github.kmu_wink.wink_official_page.global.exception.ApiException
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.AuthCookieNames
import com.github.kmu_wink.wink_official_page.global.security.SecurityConfig
import com.github.kmu_wink.wink_official_page.global.security.authentication.UserAuthentication
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class JwtFilter(
    private val jwtUtil: JwtUtil,
    private val repository: UserRepository,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = extractToken(request)

        try {
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                val id = jwtUtil.extractToken(accessToken)
                val user = repository.findById(id).orElseThrow(AuthExceptionCode.AUTHENTICATION_FAILED::toException)
                val authentication = UserAuthentication(user)
                authentication.isAuthenticated = true
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (_: TokenExpiredException) {
            if (isPublicRequest(request)) {
                filterChain.doFilter(request, response)
                return
            }
            handleException(response, AuthExceptionCode.ACCESS_TOKEN_EXPIRED.toException(), HttpStatus.UNAUTHORIZED)
            return
        } catch (e: ApiException) {
            if (isPublicRequest(request)) {
                filterChain.doFilter(request, response)
                return
            }
            handleException(response, e, HttpStatus.UNAUTHORIZED)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader("Authorization")
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7)
        }

        return request.cookies
            ?.firstOrNull { it.name == AuthCookieNames.ACCESS_TOKEN }
            ?.value
    }

    private fun handleException(response: HttpServletResponse, exception: ApiException, status: HttpStatus) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer, ApiResponse.error<Nothing>(exception.message ?: "인증에 실패했습니다."))
        response.writer.flush()
    }

    private fun isPublicRequest(request: HttpServletRequest): Boolean =
        SecurityConfig.isPublicEndpoint(request.method, request.servletPath)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.servletPath.equals("/auth/refresh-token", ignoreCase = true) ||
            request.servletPath.equals("/auth/logout", ignoreCase = true)
}
