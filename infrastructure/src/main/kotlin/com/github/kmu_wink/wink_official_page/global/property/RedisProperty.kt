package com.github.kmu_wink.wink_official_page.global.property

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Validated
class RedisProperty {
    @field:NotBlank
    var host: String = ""

    @field:Min(1)
    @field:Max(65535)
    var port: Int = 0

    var password: String? = null
}
