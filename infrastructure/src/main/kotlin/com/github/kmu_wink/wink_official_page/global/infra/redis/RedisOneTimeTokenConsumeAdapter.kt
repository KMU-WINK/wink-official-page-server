package com.github.kmu_wink.wink_official_page.global.infra.redis

import com.github.kmu_wink.wink_official_page.application.port.out.OneTimeTokenConsumePort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.OauthLoginRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PasswordResetTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RefreshTokenRepository
import com.github.kmu_wink.wink_official_page.domain.application.schema.OauthLogin
import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import com.github.kmu_wink.wink_official_page.domain.auth.schema.RefreshToken
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class RedisOneTimeTokenConsumeAdapter(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val oauthLoginRepository: OauthLoginRepository,
    private val stringRedisTemplate: StringRedisTemplate,
) : OneTimeTokenConsumePort {
    override fun consumeRefreshToken(token: String): RefreshToken? =
        consume(
            token = token,
            findByToken = refreshTokenRepository::findByToken,
            keyspace = "refresh_token",
            idOf = { it.id },
        )

    override fun consumePasswordResetToken(token: String): PasswordResetToken? =
        consume(
            token = token,
            findByToken = passwordResetTokenRepository::findByToken,
            keyspace = "password_reset_token",
            idOf = { it.id },
        )

    override fun consumeOauthLogin(token: String): OauthLogin? =
        consume(
            token = token,
            findByToken = oauthLoginRepository::findByToken,
            keyspace = "oauth_login",
            idOf = { it.id },
        )

    private fun <T : Any> consume(
        token: String,
        findByToken: (String) -> java.util.Optional<T>,
        keyspace: String,
        idOf: (T) -> Long?,
    ): T? {
        repeat(2) {
            val entity = findByToken(token).orElse(null) ?: return null
            val id = idOf(entity) ?: return null
            val entityKey = "$keyspace:$id"
            val claimed = stringRedisTemplate.execute(
                CONSUME_SCRIPT,
                listOf(
                    entityKey,
                    keyspace,
                    "$entityKey:idx",
                    "$entityKey:phantom",
                ),
                id.toString(),
            ) == 1L

            if (claimed) {
                return entity
            }
        }

        return null
    }

    private companion object {
        val CONSUME_SCRIPT = DefaultRedisScript<Long>(
            """
            local existed = redis.call('EXISTS', KEYS[1])
            local indexKeys = redis.call('SMEMBERS', KEYS[3])
            for _, indexKey in ipairs(indexKeys) do
                redis.call('SREM', indexKey, ARGV[1])
            end
            redis.call('DEL', KEYS[3])
            redis.call('SREM', KEYS[2], ARGV[1])
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[4])
            return existed
            """.trimIndent(),
            Long::class.javaObjectType,
        )
    }
}
