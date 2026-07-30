package com.github.kmu_wink.wink_official_page.global.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.github.kmu_wink.wink_official_page.application.port.out.TokenPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RefreshTokenRepository
import com.github.kmu_wink.wink_official_page.domain.auth.schema.RefreshToken
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.property.JwtProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Configuration
class JwtUtil(
    private val jwtProperty: JwtProperty,
    private val refreshTokenRepository: RefreshTokenRepository,
) : TokenPort {
    @Bean
    fun algorithm(): Algorithm = Algorithm.HMAC256(jwtProperty.key)

    override fun generateAccessToken(user: User): String = generateAccessToken(requireNotNull(user.id))

    override fun generateAccessToken(userId: String): String =
        JWT.create()
            .withIssuer("WINK")
            .withSubject("access-token")
            .withAudience("wink-official-page")
            .withExpiresAt(Instant.now().plus(jwtProperty.accessTokenExpirationHours.toLong(), ChronoUnit.HOURS))
            .withIssuedAt(Instant.now())
            .withClaim("id", userId)
            .sign(algorithm())

    override fun generateRefreshToken(user: User): String = generateRefreshToken(requireNotNull(user.id))

    override fun generateRefreshToken(userId: String): String {
        val token = JWT.create()
            .withIssuer("WINK")
            .withSubject("refresh-token")
            .withAudience("wink-official-page")
            .withExpiresAt(Instant.now().plus(jwtProperty.refreshTokenExpirationHours.toLong(), ChronoUnit.HOURS))
            .withIssuedAt(Instant.now())
            .withJWTId(UUID.randomUUID().toString())
            .sign(algorithm())

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                token = token,
                ttl = jwtProperty.refreshTokenExpirationHours.toLong(),
            ),
        )

        return token
    }

    fun extractToken(token: String): String = JWT.require(algorithm()).build().verify(token).getClaim("id").asString()

    @Throws(TokenExpiredException::class)
    fun validateToken(token: String?): Boolean {
        if (token == null) {
            return false
        }

        return try {
            JWT.require(algorithm())
                .withIssuer("WINK")
                .withSubject("access-token")
                .withAudience("wink-official-page")
                .build()
                .verify(token)
            true
        } catch (e: TokenExpiredException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }
}
