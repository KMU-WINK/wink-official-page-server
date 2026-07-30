package com.github.kmu_wink.wink_official_page.domain.recruit.privacy

import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.outbox.RecruitSmsOutbox
import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography
import com.github.kmu_wink.wink_official_page.global.module.email.outbox.MailOutbox
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.RecruitDocument
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.RecruitFormDocument
import com.mongodb.DBRef
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import org.bson.Document
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.regex.Pattern

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(
    prefix = "app.recruit.privacy.migration",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class RecruitPrivacyMigrationRunner(
    private val mongoTemplate: MongoTemplate,
    private val cryptography: RecruitPiiCryptography,
    @Value("\${app.recruit.privacy.migration.batch-size:100}")
    batchSize: Int,
    @Value("\${app.recruit.privacy.retention.zone:Asia/Seoul}")
    zone: String,
) : ApplicationRunner {
    private val batchSize = batchSize.also {
        require(it > 0) { "Recruit privacy migration batch size must be positive" }
    }
    private val zoneId = ZoneId.of(zone)
    private val claimedBlindIndexes = mutableSetOf<String>()

    override fun run(args: ApplicationArguments) {
        val migratedForms = migrateRecruitForms()
        val migratedMailOutboxes = migratePendingMailOutboxes()
        val migratedSmsOutboxes = migratePendingSmsOutboxes()
        val terminalMailOutboxes = backfillTerminalMailOutboxRetention()
        val terminalSmsOutboxes = backfillTerminalSmsOutboxRetention()

        log.info(
            "Recruit privacy migration completed; forms={}, pendingMailOutboxes={}, " +
                "pendingSmsOutboxes={}, terminalMailOutboxes={}, terminalSmsOutboxes={}, " +
                "duplicateStudentIdBlindIndexesSkipped={}, duplicateEmailBlindIndexesSkipped={}, " +
                "duplicatePhoneNumberBlindIndexesSkipped={}",
            migratedForms,
            migratedMailOutboxes,
            migratedSmsOutboxes,
            terminalMailOutboxes,
            terminalSmsOutboxes,
            duplicateStudentIdBlindIndexesSkipped,
            duplicateEmailBlindIndexesSkipped,
            duplicatePhoneNumberBlindIndexesSkipped,
        )
    }

    private fun migrateRecruitForms(): Long {
        val collection = collection(RecruitFormDocument::class.java)
        var migratedCount = 0L

        while (true) {
            val documents = collection.find(
                Filters.ne("piiSchemaVersion", RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION),
            )
                .sort(Sorts.ascending("_id"))
                .limit(batchSize)
                .into(mutableListOf())
            if (documents.isEmpty()) {
                return migratedCount
            }

            documents.forEach { document ->
                if (migrateRecruitForm(collection, document)) {
                    migratedCount++
                }
            }
        }
    }

    private fun migrateRecruitForm(
        collection: MongoCollection<Document>,
        document: Document,
    ): Boolean {
        validatePiiSchemaVersion(document["piiSchemaVersion"])
        val id = requireDocumentId(document)
        val recruitReferenceId = recruitReferenceId(document)
        val recruit = resolveRecruit(recruitReferenceId)

        val requiredPlaintext = REQUIRED_RECRUIT_FORM_STRING_FIELDS.associateWith { field ->
            requiredPlaintext(document, field)
        }
        val updates = mutableListOf<Bson>()

        requiredPlaintext.forEach { (field, plaintext) ->
            updates += Updates.set(field, cryptography.encrypt(field, plaintext))
        }
        OPTIONAL_RECRUIT_FORM_STRING_FIELDS.forEach { field ->
            document[field]?.let { storedValue ->
                val value = storedValue as? String
                    ?: invalidDocument("$field must be a string")
                updates += Updates.set(field, cryptography.encrypt(field, value))
            }
        }

        updates += Updates.set(
            "outings",
            encryptedStringList(document, "outings", allowEmpty = true),
        )
        val interviewDates = interviewDates(document)
        updates += Updates.set(
            "interviewDates",
            interviewDates.map { date ->
                cryptography.encrypt("interviewDates", date.toString())
            },
        )
        if (INTERVIEW_SCHEDULE_SENTINEL in interviewDates) {
            updates += Updates.set(
                "whyCannotInterview",
                cryptography.encrypt(
                    "whyCannotInterview",
                    RecruitPrivacyPolicy.INTERVIEW_SCHEDULE_COORDINATION_REQUIRED,
                ),
            )
        } else {
            updates += Updates.unset("whyCannotInterview")
        }
        val blindIndexUpdates = linkedMapOf<String, String>()
        addUniqueBlindIndexMigration(
            collection,
            id,
            recruitReferenceId,
            "studentIdBlindIndex",
            cryptography.studentIdBlindIndex(requiredPlaintext.getValue("studentId")),
            updates,
            blindIndexUpdates,
        )
        addUniqueBlindIndexMigration(
            collection,
            id,
            recruitReferenceId,
            "emailBlindIndex",
            cryptography.emailBlindIndex(requiredPlaintext.getValue("email")),
            updates,
            blindIndexUpdates,
        )
        addUniqueBlindIndexMigration(
            collection,
            id,
            recruitReferenceId,
            "phoneNumberBlindIndex",
            cryptography.phoneNumberBlindIndex(requiredPlaintext.getValue("phoneNumber")),
            updates,
            blindIndexUpdates,
        )

        addEditTokenMigration(document, recruit, updates)
        updates += Updates.unset("editToken")
        updates += Updates.set(
            "piiSchemaVersion",
            RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION,
        )

        val result = updateRecruitFormMigration(collection, id, updates, blindIndexUpdates)
        return result.modifiedCount == 1L
    }

    private fun updateRecruitFormMigration(
        collection: MongoCollection<Document>,
        documentId: Any,
        baseUpdates: MutableList<Bson>,
        blindIndexUpdates: MutableMap<String, String>,
    ) = retryDuplicateBlindIndexConflicts(documentId, baseUpdates, blindIndexUpdates) {
        collection.updateOne(
            Filters.and(
                Filters.eq("_id", documentId),
                Filters.ne("piiSchemaVersion", RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION),
            ),
            Updates.combine(
                baseUpdates + blindIndexUpdates.map { (field, blindIndex) ->
                    Updates.set(field, blindIndex)
                },
            ),
        )
    }

    private fun <T> retryDuplicateBlindIndexConflicts(
        documentId: Any,
        baseUpdates: MutableList<Bson>,
        blindIndexUpdates: MutableMap<String, String>,
        block: () -> T,
    ): T {
        while (true) {
            try {
                return block()
            } catch (exception: MongoWriteException) {
                val duplicateField = duplicateBlindIndexField(exception) ?: throw exception
                if (blindIndexUpdates.remove(duplicateField) == null) {
                    throw exception
                }
                baseUpdates += Updates.unset(duplicateField)
                incrementDuplicateBlindIndexSkipped(duplicateField)
                log.warn(
                    "Skipped duplicate legacy recruit form blind index after Mongo unique conflict; " +
                        "formId={}, field={}",
                    documentId,
                    duplicateField,
                )
            }
        }
    }

    private fun addUniqueBlindIndexMigration(
        collection: MongoCollection<Document>,
        documentId: Any,
        recruitReferenceId: Any,
        field: String,
        blindIndex: String,
        updates: MutableList<Bson>,
        blindIndexUpdates: MutableMap<String, String>,
    ) {
        val claimKey = "$recruitReferenceId\u0000$field\u0000$blindIndex"
        val alreadyClaimedInThisRun = !claimedBlindIndexes.add(claimKey)
        val duplicate = collection.find(
            Filters.and(
                Filters.eq("recruit.\$id", recruitReferenceId),
                Filters.eq(field, blindIndex),
                Filters.ne("_id", documentId),
            ),
        ).limit(1).first()

        if (!alreadyClaimedInThisRun && duplicate == null) {
            blindIndexUpdates[field] = blindIndex
            return
        }

        updates += Updates.unset(field)
        incrementDuplicateBlindIndexSkipped(field)
        log.warn(
            "Skipped duplicate legacy recruit form blind index; formId={}, duplicateFormId={}, field={}",
            documentId,
            duplicate?.get("_id") ?: "[claimed earlier in this migration run]",
            field,
        )
    }

    private fun duplicateBlindIndexField(exception: MongoWriteException): String? =
        when {
            exception.message?.contains("uq_recruit_form_recruit_student_id_blind_index") == true ->
                "studentIdBlindIndex"
            exception.message?.contains("uq_recruit_form_recruit_email_blind_index") == true ->
                "emailBlindIndex"
            exception.message?.contains("uq_recruit_form_recruit_phone_number_blind_index") == true ->
                "phoneNumberBlindIndex"
            else -> null
        }

    private fun incrementDuplicateBlindIndexSkipped(field: String) {
        when (field) {
            "studentIdBlindIndex" -> duplicateStudentIdBlindIndexesSkipped++
            "emailBlindIndex" -> duplicateEmailBlindIndexesSkipped++
            "phoneNumberBlindIndex" -> duplicatePhoneNumberBlindIndexesSkipped++
        }
    }

    private fun addEditTokenMigration(
        document: Document,
        recruit: RecruitDocument,
        updates: MutableList<Bson>,
    ) {
        val legacyStoredToken = document["editToken"]
        val existingDigest = document["editTokenDigest"]
        val hasEditCredential = legacyStoredToken != null || existingDigest != null
        if (!hasEditCredential) {
            return
        }

        val recruitEndDate = recruit.recruitEndDate
            ?: invalidDocument("recruit end date is required for edit-token expiry")
        val expiresAt = recruitEndDate.atTime(LocalTime.MAX)
        updates += Updates.set("editTokenExpiresAt", expiresAt.toBsonDate(zoneId))

        if (legacyStoredToken != null) {
            val storedToken = legacyStoredToken as? String
                ?: invalidDocument("editToken must be a string")
            val rawToken = cryptography.decrypt("editToken", storedToken)
            if (rawToken.isBlank()) {
                invalidDocument("editToken must not be blank")
            }
            updates += Updates.set("editTokenDigest", cryptography.editTokenDigest(rawToken))
            return
        }

        val digest = existingDigest as? String
            ?: invalidDocument("editTokenDigest must be a string")
        if (!EDIT_TOKEN_DIGEST_PATTERN.matches(digest)) {
            invalidDocument("editTokenDigest has an invalid format")
        }
    }

    private fun migratePendingMailOutboxes(): Long =
        migratePendingOutboxes(
            collection = collection(MailOutbox::class.java),
            requiredEncryptedFields = MAIL_OUTBOX_REQUIRED_ENCRYPTED_FIELDS,
            optionalEncryptedFields = OUTBOX_OPTIONAL_ENCRYPTED_FIELDS,
        )

    private fun migratePendingSmsOutboxes(): Long =
        migratePendingOutboxes(
            collection = collection(RecruitSmsOutbox::class.java),
            requiredEncryptedFields = SMS_OUTBOX_REQUIRED_ENCRYPTED_FIELDS,
            optionalEncryptedFields = OUTBOX_OPTIONAL_ENCRYPTED_FIELDS,
        )

    private fun migratePendingOutboxes(
        collection: MongoCollection<Document>,
        requiredEncryptedFields: List<String>,
        optionalEncryptedFields: List<String>,
    ): Long {
        var migratedCount = 0L
        val encryptedFields = requiredEncryptedFields + optionalEncryptedFields
        val hasPlaintext = Filters.or(
            encryptedFields.map { field ->
                Filters.regex(field, UNPROTECTED_NON_EMPTY_VALUE_PATTERN)
            },
        )

        while (true) {
            val documents = collection.find(
                Filters.and(
                    Filters.eq("status", OUTBOX_PENDING_STATUS),
                    hasPlaintext,
                ),
            )
                .sort(Sorts.ascending("_id"))
                .limit(batchSize)
                .into(mutableListOf())
            if (documents.isEmpty()) {
                return migratedCount
            }

            documents.forEach { document ->
                val id = requireDocumentId(document)
                val storedValues = requiredEncryptedFields.associateWith { field ->
                    val value = document[field] as? String
                        ?: invalidDocument("$field must be a string in a pending outbox")
                    if (value.isBlank()) {
                        invalidDocument("$field must not be blank in a pending outbox")
                    }
                    value
                }.toMutableMap()
                optionalEncryptedFields.forEach { field ->
                    document[field]?.let { storedValue ->
                        val value = storedValue as? String
                            ?: invalidDocument("$field must be a string in a pending outbox")
                        if (value.isNotBlank()) {
                            storedValues[field] = value
                        }
                    }
                }
                val optimisticFilter = mutableListOf<Bson>(
                    Filters.eq("_id", id),
                    Filters.eq("status", OUTBOX_PENDING_STATUS),
                )
                if (document.containsKey("updatedAt")) {
                    optimisticFilter += Filters.eq("updatedAt", document["updatedAt"])
                } else {
                    optimisticFilter += Filters.exists("updatedAt", false)
                }
                val updates = storedValues.map { (field, value) ->
                    Updates.set(field, cryptography.encrypt(field, value))
                }
                val result = collection.updateOne(
                    Filters.and(optimisticFilter),
                    Updates.combine(updates),
                )
                migratedCount += result.modifiedCount
            }
        }
    }

    private fun backfillTerminalMailOutboxRetention(): Long =
        backfillTerminalOutboxRetention(
            collection = collection(MailOutbox::class.java),
            scrubUpdates = listOf(
                Updates.set("recipient", ""),
                Updates.set("title", ""),
                Updates.set("html", ""),
                Updates.set("barrierToken", ""),
                Updates.set("lastError", ""),
            ),
        )

    private fun backfillTerminalSmsOutboxRetention(): Long =
        backfillTerminalOutboxRetention(
            collection = collection(RecruitSmsOutbox::class.java),
            scrubUpdates = listOf(
                Updates.set("target", ""),
                Updates.set("content", ""),
                Updates.unset("recruitFormId"),
                Updates.set("lastError", ""),
            ),
        )

    private fun backfillTerminalOutboxRetention(
        collection: MongoCollection<Document>,
        scrubUpdates: List<Bson>,
    ): Long {
        var migratedCount = 0L
        var invalidTimestampCount = 0L
        val terminalWithoutPurgeAt = Filters.and(
            Filters.`in`("status", OUTBOX_TERMINAL_STATUSES),
            Filters.or(
                Filters.exists("purgeAt", false),
                Filters.eq("purgeAt", null),
            ),
        )

        while (true) {
            val documents = collection.find(terminalWithoutPurgeAt)
                .sort(Sorts.ascending("_id"))
                .limit(batchSize)
                .into(mutableListOf())
            if (documents.isEmpty()) {
                if (invalidTimestampCount > 0) {
                    log.warn(
                        "Terminal outbox retention used the current time for documents with " +
                            "invalid timestamps; count={}",
                        invalidTimestampCount,
                    )
                }
                return migratedCount
            }

            documents.forEach { document ->
                val id = requireDocumentId(document)
                val status = document["status"] as? String
                    ?: invalidDocument("terminal outbox status must be a string")
                val terminalAt = terminalTimestamp(document) ?: LocalDateTime.now().also {
                    invalidTimestampCount++
                }
                val updates = scrubUpdates + Updates.set(
                    "purgeAt",
                    terminalAt.plusDays(OUTBOX_TERMINAL_RETENTION_DAYS).toBsonDate(zoneId),
                )
                val result = collection.updateOne(
                    Filters.and(
                        Filters.eq("_id", id),
                        Filters.eq("status", status),
                        Filters.or(
                            Filters.exists("purgeAt", false),
                            Filters.eq("purgeAt", null),
                        ),
                    ),
                    Updates.combine(updates),
                )
                migratedCount += result.modifiedCount
            }
        }
    }

    private fun encryptedStringList(
        document: Document,
        field: String,
        allowEmpty: Boolean,
    ): List<String> {
        val values = document[field] as? List<*>
            ?: invalidDocument("$field must be a list")
        if (!allowEmpty && values.isEmpty()) {
            invalidDocument("$field must not be empty")
        }
        return values.map { element ->
            val value = element as? String
                ?: invalidDocument("$field contains a non-string value")
            if (value.isBlank()) {
                invalidDocument("$field contains a blank value")
            }
            cryptography.encrypt(field, value)
        }
    }

    private fun interviewDates(document: Document): List<LocalDate> {
        val values = document["interviewDates"] as? List<*>
            ?: invalidDocument("interviewDates must be a list")
        if (values.isEmpty()) {
            invalidDocument("interviewDates must not be empty")
        }
        return values.map { element ->
            val date = when (element) {
                is LocalDate -> element
                is LocalDateTime -> element.toLocalDate()
                is Date -> element.toInstant().atZone(zoneId).toLocalDate()
                is String -> try {
                    LocalDate.parse(cryptography.decrypt("interviewDates", element))
                } catch (_: DateTimeParseException) {
                    invalidDocument("interviewDates contains an invalid date")
                }
                else -> invalidDocument("interviewDates contains an unsupported value")
            }
            date
        }
    }

    private fun requiredPlaintext(document: Document, field: String): String {
        val storedValue = document[field]
            ?: invalidDocument("$field is missing")
        val plaintext = when (storedValue) {
            is String -> cryptography.decrypt(field, storedValue)
            else -> legacyNumericStudentIdPlaintext(field, storedValue)
        }
        if (plaintext.isBlank()) {
            invalidDocument("$field must not be blank")
        }
        return plaintext
    }

    private fun legacyNumericStudentIdPlaintext(field: String, storedValue: Any): String {
        if (field != "studentId") {
            invalidDocument("$field must be a string")
        }

        val value = when (storedValue) {
            is Byte -> storedValue.toLong()
            is Short -> storedValue.toLong()
            is Int -> storedValue.toLong()
            is Long -> storedValue
            else -> invalidDocument("studentId must be a string or a legacy integral number")
        }
        if (value < 0) {
            invalidDocument("studentId must not be negative")
        }
        return value.toString()
    }

    private fun recruitReferenceId(document: Document): Any =
        when (val reference = document["recruit"]) {
            is DBRef -> reference.id
            is Document -> reference["\$id"]
            else -> null
        } ?: invalidDocument("recruit reference is missing or invalid")

    private fun resolveRecruit(referenceId: Any): RecruitDocument {
        return mongoTemplate.findById(referenceId, RecruitDocument::class.java)
            ?: invalidDocument("referenced recruit does not exist")
    }

    private fun validatePiiSchemaVersion(storedVersion: Any?) {
        if (storedVersion == null) {
            return
        }
        val versionNumber = storedVersion as? Number
            ?: invalidDocument("piiSchemaVersion must be a number")
        val version = versionNumber.toInt()
        if (version < 0 || versionNumber.toDouble() != version.toDouble()) {
            invalidDocument("piiSchemaVersion must be a non-negative integer")
        }
        if (version >= RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION) {
            invalidDocument("piiSchemaVersion is not supported by this application")
        }
    }

    private fun terminalTimestamp(document: Document): LocalDateTime? =
        listOf("processedAt", "failedAt", "updatedAt", "createdAt")
            .firstNotNullOfOrNull { field -> document[field].asLocalDateTime(zoneId) }

    private fun requireDocumentId(document: Document): Any =
        document["_id"] ?: invalidDocument("_id is missing")

    private fun <T : Any> collection(type: Class<T>): MongoCollection<Document> =
        mongoTemplate.getCollection(mongoTemplate.getCollectionName(type))

    private fun invalidDocument(reason: String): Nothing =
        throw IllegalStateException("Recruit privacy migration rejected an invalid document: $reason")

    private companion object {
        const val OUTBOX_PENDING_STATUS = "PENDING"
        const val OUTBOX_TERMINAL_RETENTION_DAYS = 30L
        val OUTBOX_TERMINAL_STATUSES = listOf("PROCESSED", "FAILED", "CANCELED")
        val REQUIRED_RECRUIT_FORM_STRING_FIELDS = listOf(
            "name",
            "studentId",
            "department",
            "email",
            "phoneNumber",
            "jiwonDonggi",
            "selfIntroduce",
        )
        val OPTIONAL_RECRUIT_FORM_STRING_FIELDS = listOf(
            "github",
            "favoriteProject",
        )
        val INTERVIEW_SCHEDULE_SENTINEL: LocalDate = LocalDate.of(1, 1, 1)
        val MAIL_OUTBOX_REQUIRED_ENCRYPTED_FIELDS = listOf("recipient", "title", "html", "barrierToken")
        val SMS_OUTBOX_REQUIRED_ENCRYPTED_FIELDS = listOf("target", "content")
        val OUTBOX_OPTIONAL_ENCRYPTED_FIELDS = listOf("lastError")
        val EDIT_TOKEN_DIGEST_PATTERN = Regex("^[A-Za-z0-9_-]{43}$")
        val UNPROTECTED_NON_EMPTY_VALUE_PATTERN = Pattern.compile(
            "^(?!${Pattern.quote(RecruitPiiCryptography.CURRENT_PREFIX)}).+",
        )
        val log = LoggerFactory.getLogger(RecruitPrivacyMigrationRunner::class.java)
    }

    private var duplicateStudentIdBlindIndexesSkipped = 0L
    private var duplicateEmailBlindIndexesSkipped = 0L
    private var duplicatePhoneNumberBlindIndexesSkipped = 0L
}

private fun LocalDateTime.toBsonDate(zoneId: ZoneId): Date =
    Date.from(atZone(zoneId).toInstant())

private fun Any?.asLocalDateTime(zoneId: ZoneId): LocalDateTime? =
    when (this) {
        is LocalDateTime -> this
        is Date -> LocalDateTime.ofInstant(toInstant(), zoneId)
        is Instant -> LocalDateTime.ofInstant(this, zoneId)
        else -> null
    }
