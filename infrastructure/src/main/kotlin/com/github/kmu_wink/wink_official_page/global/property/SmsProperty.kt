package com.github.kmu_wink.wink_official_page.global.property

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "app.sms")
@Validated
class SmsProperty {
    @field:NotBlank
    var id: String = ""

    @field:NotBlank
    var pw: String = ""

    @field:NotBlank
    var sendPhone: String = ""
}
