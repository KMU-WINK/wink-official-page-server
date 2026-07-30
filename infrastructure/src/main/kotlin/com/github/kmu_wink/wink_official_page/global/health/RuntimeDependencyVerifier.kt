package com.github.kmu_wink.wink_official_page.global.health

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RuntimeDependencyVerifier(
    private val mongoTemplate: MongoTemplate,
    private val redisTemplate: StringRedisTemplate,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        mongoTemplate.executeCommand("""{"ping": 1}""")

        val redisPing = redisTemplate.execute<String> { connection ->
            connection.commands().ping()
        }
        check(redisPing.equals("PONG", ignoreCase = true)) {
            "Redis readiness check did not return PONG"
        }
    }
}
