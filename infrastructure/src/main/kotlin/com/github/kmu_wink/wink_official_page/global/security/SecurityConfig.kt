package com.github.kmu_wink.wink_official_page.global.security

import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import com.github.kmu_wink.wink_official_page.global.security.jwt.JwtFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.util.AntPathMatcher
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val authenticationProvider: AuthenticationProvider,
    private val jwtFilter: JwtFilter,
    private val publicEndpointRateLimitFilter: PublicEndpointRateLimitFilter,
    private val objectMapper: ObjectMapper,
    @Value("\${app.auth.cookie-secure:false}")
    private val secureCookies: Boolean,
) {
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val csrfTokenRequestHandler = CsrfTokenRequestAttributeHandler()
        csrfTokenRequestHandler.setCsrfRequestAttributeName(null)
        val csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse().apply {
            setCookieCustomizer { cookie -> cookie.secure(secureCookies).sameSite("Lax") }
        }

        return http
            .cors { }
            .csrf {
                it.csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(csrfTokenRequestHandler)
                    .ignoringRequestMatchers("/application/oauth/token")
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    writeError(response, HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")
                }
                it.accessDeniedHandler { _, response, _ ->
                    writeError(response, HttpStatus.FORBIDDEN, "권한이 없습니다.")
                }
            }
            .addFilterAfter(CsrfCookieFilter(), CsrfFilter::class.java)
            .addFilterBefore(publicEndpointRateLimitFilter, CsrfFilter::class.java)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, *PUBLIC_GET_ENDPOINTS).permitAll()
                    .requestMatchers(HttpMethod.POST, *PUBLIC_POST_ENDPOINTS).permitAll()
                    .requestMatchers(*PUBLIC_ANY_METHOD_ENDPOINTS).permitAll()
                    .anyRequest().authenticated()
            }
            .build()
    }

    private fun writeError(response: HttpServletResponse, status: HttpStatus, message: String) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer, ApiResponse.error<Nothing>(message))
        response.writer.flush()
    }

    companion object {
        val PUBLIC_GET_ENDPOINTS = arrayOf(
            "/health/ready",
            "/auth/csrf",
            "/program/activity",
            "/program/history",
            "/program/project",
            "/program/study/**",
            "/recruit/latest",
            "/user",
            "/swagger-ui/**",
            "/v3/api-docs/**",
        )
        val PUBLIC_POST_ENDPOINTS = arrayOf(
            "/auth/login",
            "/auth/logout",
            "/auth/register",
            "/auth/register/check",
            "/auth/refresh-token",
            "/auth/reset-password",
            "/auth/reset-password/request",
            "/auth/reset-password/check",
            "/application/oauth/token",
            "/recruit/*",
            "/recruit/*/check/studentId",
            "/recruit/*/check/email",
            "/recruit/*/check/phoneNumber",
            "/recruit/edit-session",
            "/recruit/edit-session/form",
        )
        val PUBLIC_ANY_METHOD_ENDPOINTS = arrayOf(
            "/error",
        )
        const val XSRF_COOKIE = AuthCookieNames.XSRF_TOKEN
        const val XSRF_HEADER = AuthCookieNames.XSRF_HEADER

        private val pathMatcher = AntPathMatcher()

        fun isPublicEndpoint(method: String, path: String): Boolean {
            if (method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
                return true
            }

            val methodPatterns = when {
                method.equals(HttpMethod.GET.name(), ignoreCase = true) -> PUBLIC_GET_ENDPOINTS
                method.equals(HttpMethod.POST.name(), ignoreCase = true) -> PUBLIC_POST_ENDPOINTS
                else -> emptyArray()
            }

            return (PUBLIC_ANY_METHOD_ENDPOINTS + methodPatterns).any { pathMatcher.match(it, path) }
        }
    }
}
