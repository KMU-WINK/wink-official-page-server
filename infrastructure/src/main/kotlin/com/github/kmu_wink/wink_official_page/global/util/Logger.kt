package com.github.kmu_wink.wink_official_page.global.util

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HexFormat

@Component
class Logger : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val start = System.currentTimeMillis()
        filterChain.doFilter(request, response)
        val elapsed = System.currentTimeMillis() - start

        log.info(
            "{} {} {} {}ms",
            request.method,
            redactSensitivePath(request.requestURI),
            clientFingerprint(request),
            elapsed,
        )
    }

    private fun clientFingerprint(request: HttpServletRequest): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(request.remoteAddr.orEmpty().toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(digest).take(16)
    }

    private fun redactSensitivePath(path: String): String =
        SENSITIVE_PATH_SEGMENT.replace(path) { result ->
            "${result.groupValues[1]}[redacted]"
        }

    companion object {
        private val log = LoggerFactory.getLogger(Logger::class.java)
        private val SENSITIVE_PATH_SEGMENT =
            Regex("(?i)(/(?:edit|register|reset-password)/)[^/]{20,}")
    }
}
