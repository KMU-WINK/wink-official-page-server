package com.github.kmu_wink.wink_official_page.global.config

import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RecruitPrivacyConfig {
    @Bean
    fun recruitPiiCryptography(
        @Value("\${app.recruit.privacy.encryption-key}")
        base64MasterKey: String,
    ): RecruitPiiCryptography = RecruitPiiCryptography.fromBase64(base64MasterKey)
}
