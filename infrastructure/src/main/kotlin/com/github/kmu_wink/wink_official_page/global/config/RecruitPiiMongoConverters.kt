package com.github.kmu_wink.wink_official_page.global.config

import com.github.kmu_wink.wink_official_page.domain.recruit.outbox.RecruitSmsOutbox
import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography
import com.github.kmu_wink.wink_official_page.global.module.email.outbox.MailOutbox
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.RecruitFormDocument
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.mongodb.core.convert.MongoConversionContext
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

internal fun recruitPiiMongoCustomConversions(
    cryptography: RecruitPiiCryptography,
): MongoCustomConversions =
    MongoCustomConversions.create { adapter ->
        adapter.configurePropertyConversions { registrar ->
            RECRUIT_FORM_STRING_PROPERTIES.forEach { propertyName ->
                registrar.registerConverter(
                    RecruitFormDocument::class.java,
                    propertyName,
                    EncryptedStringPropertyConverter(propertyName, cryptography),
                )
            }
            registrar.registerConverter(
                RecruitFormDocument::class.java,
                "outings",
                EncryptedStringListPropertyConverter("outings", cryptography),
            )
            registrar.registerConverter(
                RecruitFormDocument::class.java,
                "interviewDates",
                EncryptedLocalDateListPropertyConverter("interviewDates", cryptography),
            )
            MAIL_OUTBOX_STRING_PROPERTIES.forEach { propertyName ->
                registrar.registerConverter(
                    MailOutbox::class.java,
                    propertyName,
                    EncryptedStringPropertyConverter(propertyName, cryptography),
                )
            }
            RECRUIT_SMS_OUTBOX_STRING_PROPERTIES.forEach { propertyName ->
                registrar.registerConverter(
                    RecruitSmsOutbox::class.java,
                    propertyName,
                    EncryptedStringPropertyConverter(propertyName, cryptography),
                )
            }
        }
    }

private class EncryptedStringPropertyConverter(
    private val propertyName: String,
    private val cryptography: RecruitPiiCryptography,
) : PropertyValueConverter<String, String, MongoConversionContext> {
    override fun read(value: String, context: MongoConversionContext): String =
        cryptography.decrypt(propertyName, value)

    override fun write(value: String, context: MongoConversionContext): String =
        cryptography.encrypt(propertyName, value)
}

private class EncryptedStringListPropertyConverter(
    private val propertyName: String,
    private val cryptography: RecruitPiiCryptography,
) : PropertyValueConverter<List<String>, List<*>, MongoConversionContext> {
    override fun read(value: List<*>, context: MongoConversionContext): List<String> =
        value.map { element ->
            val storedValue = element as? String
                ?: throw IllegalStateException("$propertyName contains a non-string value")
            cryptography.decrypt(propertyName, storedValue)
        }

    override fun write(value: List<String>, context: MongoConversionContext): List<String> =
        value.map { cryptography.encrypt(propertyName, it) }
}

private class EncryptedLocalDateListPropertyConverter(
    private val propertyName: String,
    private val cryptography: RecruitPiiCryptography,
) : PropertyValueConverter<List<LocalDate>, List<*>, MongoConversionContext> {
    override fun read(value: List<*>, context: MongoConversionContext): List<LocalDate> =
        value.map { element ->
            when (element) {
                is LocalDate -> element
                is LocalDateTime -> element.toLocalDate()
                is Date -> element.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                is String -> LocalDate.parse(cryptography.decrypt(propertyName, element))
                else -> throw IllegalStateException("$propertyName contains an unsupported date value")
            }
        }

    override fun write(value: List<LocalDate>, context: MongoConversionContext): List<String> =
        value.map { cryptography.encrypt(propertyName, it.toString()) }
}

private val RECRUIT_FORM_STRING_PROPERTIES = setOf(
    "name",
    "studentId",
    "department",
    "email",
    "phoneNumber",
    "jiwonDonggi",
    "selfIntroduce",
    "whyCannotInterview",
    "github",
    "favoriteProject",
)

private val MAIL_OUTBOX_STRING_PROPERTIES = setOf(
    "recipient",
    "title",
    "html",
    "barrierToken",
    "lastError",
)

private val RECRUIT_SMS_OUTBOX_STRING_PROPERTIES = setOf(
    "target",
    "content",
    "lastError",
)
