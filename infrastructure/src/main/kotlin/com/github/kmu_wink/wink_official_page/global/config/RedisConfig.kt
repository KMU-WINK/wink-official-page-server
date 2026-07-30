package com.github.kmu_wink.wink_official_page.global.config

import com.github.kmu_wink.wink_official_page.global.property.RedisProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.redis.connection.RedisConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories(
    basePackages = ["com.github.kmu_wink.wink_official_page"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [
                ".*\\.repository\\..*Redis.*",
                ".*Spring(OauthLogin|PasswordResetToken|RefreshToken)DocumentRepository",
            ],
        ),
    ],
)
class RedisConfig(
    private val redisProperty: RedisProperty,
) {
    @Bean
    fun redisConfiguration(): RedisConfiguration {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration()
        redisStandaloneConfiguration.hostName = redisProperty.host
        redisStandaloneConfiguration.port = redisProperty.port
        redisProperty.password?.takeIf(String::isNotBlank)?.let { redisStandaloneConfiguration.setPassword(it) }
        return redisStandaloneConfiguration
    }

    @Bean
    fun connectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(redisConfiguration())
}
