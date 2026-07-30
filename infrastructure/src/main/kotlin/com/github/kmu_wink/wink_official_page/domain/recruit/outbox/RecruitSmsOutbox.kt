package com.github.kmu_wink.wink_official_page.domain.recruit.outbox

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "recruit_sms_outbox")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_recruit_sms_outbox_pending_claim",
        def = "{'status': 1, 'nextAttemptAt': 1, 'createdAt': 1}",
    ),
    CompoundIndex(
        name = "idx_recruit_sms_outbox_stale_lock",
        def = "{'status': 1, 'lockedAt': 1}",
    ),
)
class RecruitSmsOutbox(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var dedupeKey: String = "",
    var recruitId: String = "",
    var requiredStep: Recruit.Step = Recruit.Step.PRE,
    var target: String = "",
    var content: String = "",
    var recruitFormId: String? = null,
    var createPreUserBeforeSend: Boolean = false,
    var status: Status = Status.PENDING,
    var attempts: Int = 0,
    var stateWaits: Int = 0,
    var nextAttemptAt: LocalDateTime = LocalDateTime.now(),
    var lockedAt: LocalDateTime? = null,
    var lockToken: String? = null,
    var processedAt: LocalDateTime? = null,
    var failedAt: LocalDateTime? = null,
    @field:Indexed(name = "ttl_recruit_sms_outbox_purge_at", expireAfter = "0s")
    var purgeAt: LocalDateTime? = null,
    var lastError: String? = null,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
) {
    enum class Status {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED,
        CANCELED,
    }
}
