package com.github.kmu_wink.wink_official_page.global.property

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
class JwtProperty {
    @field:NotBlank
    @field:Size(min = 32)
    var key: String = ""

    @field:Min(1)
    var accessTokenExpirationHours: Int = 0

    @field:Min(1)
    var refreshTokenExpirationHours: Int = 0
}
