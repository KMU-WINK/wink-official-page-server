package com.github.kmu_wink.wink_official_page.global.security

import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.util.Base64
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class PublicEndpointRateLimitFilter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    @org.springframework.beans.factory.annotation.Value("\${app.security.proxy-client-hmac-key}")
    encodedProxyClientHmacKey: String,
) : OncePerRequestFilter() {
    private val proxyClientHmacKey = decodeProxyClientHmacKey(encodedProxyClientHmacKey)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        policyFor(request) == null

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val policy = policyFor(request) ?: return filterChain.doFilter(request, response)
        val key = "rate-limit:recruit:${policy.scope}:${clientFingerprint(request)}"

        try {
            val attempts = redisTemplate.opsForValue().increment(key) ?: 1L
            if (attempts == 1L) {
                redisTemplate.expire(key, policy.window)
            }

            if (attempts > policy.limit) {
                val retryAfter = redisTemplate.getExpire(key)
                    .coerceAtLeast(1)
                response.setHeader("Retry-After", retryAfter.toString())
                writeError(response, HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
                return
            }
        } catch (_: DataAccessException) {
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE, "요청 보호 서비스를 사용할 수 없습니다.")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun writeError(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String,
    ) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer, ApiResponse.error<Nothing>(message))
        response.writer.flush()
    }

    private fun policyFor(request: HttpServletRequest): RateLimitPolicy? {
        if (!request.method.equals("POST", ignoreCase = true)) {
            return null
        }

        val path = request.requestURI
        return when {
            path == "/auth/login" ->
                RateLimitPolicy("auth-login", 10, Duration.ofMinutes(1))
            path == "/auth/reset-password/request" ->
                RateLimitPolicy("password-reset-request", 5, Duration.ofMinutes(10))
            path == "/auth/register/check" || path == "/auth/reset-password/check" ->
                RateLimitPolicy("one-time-token-check", 20, Duration.ofMinutes(1))
            path == "/auth/register" || path == "/auth/reset-password" ->
                RateLimitPolicy("one-time-token-consume", 5, Duration.ofMinutes(10))
            DUPLICATION_CHECK_PATH.matches(path) ->
                RateLimitPolicy("duplication-check", 30, Duration.ofMinutes(1))
            path == "/recruit/edit-session" || path == "/recruit/edit-session/form" ->
                RateLimitPolicy("edit-session", 20, Duration.ofMinutes(1))
            SUBMISSION_PATH.matches(path) && path != "/recruit/edit-session" ->
                RateLimitPolicy("submission", 5, Duration.ofMinutes(10))
            else -> null
        }
    }

    private fun digest(value: String): String =
        HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(StandardCharsets.UTF_8)),
        ).take(24)

    private fun clientFingerprint(request: HttpServletRequest): String {
        val signedContext = request.getHeader(PROXY_CLIENT_CONTEXT_HEADER)
        val verifiedAddress = signedContext?.let(::verifyProxyClientContext)
        return digest(verifiedAddress ?: request.remoteAddr.orEmpty())
    }

    private fun verifyProxyClientContext(context: String): String? {
        val parts = context.split('.', limit = 2)
        if (parts.size != 2) {
            return null
        }

        return runCatching {
            val addressBytes = Base64.getUrlDecoder().decode(parts[0])
            val suppliedSignature = Base64.getUrlDecoder().decode(parts[1])
            val expectedSignature = Mac.getInstance(HMAC_ALGORITHM).run {
                init(SecretKeySpec(proxyClientHmacKey, HMAC_ALGORITHM))
                doFinal(addressBytes)
            }
            if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
                return null
            }

            String(addressBytes, StandardCharsets.UTF_8)
                .takeIf { CLIENT_ADDRESS.matches(it) }
        }.getOrNull()
    }

    private fun decodeProxyClientHmacKey(value: String): ByteArray {
        val decoded = runCatching { Base64.getDecoder().decode(value) }
            .getOrElse {
                throw IllegalArgumentException(
                    "RECRUIT_PROXY_CLIENT_HMAC_KEY must be valid Base64",
                    it,
                )
            }
        require(decoded.size == PROXY_CLIENT_HMAC_KEY_BYTES) {
            "RECRUIT_PROXY_CLIENT_HMAC_KEY must decode to exactly $PROXY_CLIENT_HMAC_KEY_BYTES bytes"
        }
        return decoded
    }

    private data class RateLimitPolicy(
        val scope: String,
        val limit: Long,
        val window: Duration,
    )

    companion object {
        private const val PROXY_CLIENT_CONTEXT_HEADER = "X-WINK-Client-Context"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val PROXY_CLIENT_HMAC_KEY_BYTES = 32
        private val CLIENT_ADDRESS = Regex("^[0-9a-fA-F:.]{3,64}$")
        private val DUPLICATION_CHECK_PATH =
            Regex("^/recruit/[^/]+/check/(studentId|email|phoneNumber)$")
        private val SUBMISSION_PATH = Regex("^/recruit/[^/]+$")
    }
}
