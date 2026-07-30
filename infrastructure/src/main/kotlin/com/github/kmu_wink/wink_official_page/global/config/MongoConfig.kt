package com.github.kmu_wink.wink_official_page.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.web.config.EnableSpringDataWebSupport
import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography

@Configuration
@EnableMongoAuditing
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableMongoRepositories(
    basePackages = ["com.github.kmu_wink.wink_official_page"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [
                ".*\\.repository\\..*Redis.*",
                ".*Spring(OauthLogin|PasswordResetToken|RefreshToken)DocumentRepository",
            ],
        ),
    ],
)
class MongoConfig {
    @Bean
    fun mongoCustomConversions(
        recruitPiiCryptography: RecruitPiiCryptography,
    ): MongoCustomConversions = recruitPiiMongoCustomConversions(recruitPiiCryptography)

    @Bean
    fun mappingMongoConverter(
        mongoDatabaseFactory: MongoDatabaseFactory,
        mongoMappingContext: MongoMappingContext,
        mongoCustomConversions: MongoCustomConversions,
    ): MappingMongoConverter {
        val dbRefResolver = DefaultDbRefResolver(mongoDatabaseFactory)
        return MappingMongoConverter(dbRefResolver, mongoMappingContext).apply {
            setCustomConversions(mongoCustomConversions)
            setCodecRegistryProvider(mongoDatabaseFactory)
            setTypeMapper(DefaultMongoTypeMapper(null))
        }
    }
}
