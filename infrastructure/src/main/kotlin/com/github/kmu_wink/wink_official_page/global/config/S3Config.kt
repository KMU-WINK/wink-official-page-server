package com.github.kmu_wink.wink_official_page.global.config

import com.github.kmu_wink.wink_official_page.global.property.AwsProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class S3Config(
    private val awsProperty: AwsProperty,
) {
    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        val accessKey = awsProperty.credential.accessKey?.takeIf(String::isNotBlank)
        val secretKey = awsProperty.credential.secretKey?.takeIf(String::isNotBlank)
        require((accessKey == null) == (secretKey == null)) {
            "AWS access key and secret key must either both be configured or both be omitted"
        }

        return if (accessKey != null && secretKey != null) {
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
        } else {
            DefaultCredentialsProvider.builder().build()
        }
    }

    @Bean
    fun s3Client(): S3Client = S3Client.builder()
        .region(Region.of(awsProperty.region))
        .credentialsProvider(awsCredentialsProvider())
        .build()

    @Bean
    fun s3Presigner(): S3Presigner = S3Presigner.builder()
        .region(Region.of(awsProperty.region))
        .credentialsProvider(awsCredentialsProvider())
        .build()
}
