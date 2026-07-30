package com.github.kmu_wink.wink_official_page.global.property

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "app.cloud.aws")
@Validated
class AwsProperty {
    @field:NotBlank
    var region: String = ""

    @field:NestedConfigurationProperty
    var s3: S3 = S3()

    @field:NestedConfigurationProperty
    var credential: Credential = Credential()

    class S3 {
        @field:NotBlank
        var bucket: String = ""

        var publicReadAclEnabled: Boolean = false
    }

    class Credential {
        var accessKey: String? = null

        var secretKey: String? = null
    }
}
