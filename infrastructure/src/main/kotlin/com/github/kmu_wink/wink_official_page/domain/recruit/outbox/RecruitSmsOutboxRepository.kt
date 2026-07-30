package com.github.kmu_wink.wink_official_page.domain.recruit.outbox

import com.github.kmu_wink.wink_official_page.domain.recruit.outbox.RecruitSmsOutbox.Status
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Repository
class RecruitSmsOutboxRepository(
    private val mongoTemplate: MongoTemplate,
) {
    private val terminalRetention: Duration = Duration.ofDays(30)

    fun enqueue(
        dedupeKey: String,
        recruitId: String,
        requiredStep: Recruit.Step,
        target: String,
        content: String,
        recruitFormId: String?,
        createPreUserBeforeSend: Boolean,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        val retryableQuery = Query.query(
            Criteria.where("dedupeKey").`is`(dedupeKey)
                .and("status").`in`(Status.FAILED, Status.CANCELED),
        )
        val retryableUpdate = Update()
            .set("recruitId", recruitId)
            .set("requiredStep", requiredStep)
            .set("target", target)
            .set("content", content)
            .set("recruitFormId", recruitFormId)
            .set("createPreUserBeforeSend", createPreUserBeforeSend)
            .set("status", Status.PENDING)
            .set("attempts", 0)
            .set("stateWaits", 0)
            .set("nextAttemptAt", now)
            .set("updatedAt", now)
            .unset("lockedAt")
            .unset("lockToken")
            .unset("processedAt")
            .unset("failedAt")
            .unset("purgeAt")
            .unset("lastError")
        mongoTemplate.updateFirst(retryableQuery, retryableUpdate, RecruitSmsOutbox::class.java)

        val query = Query.query(Criteria.where("dedupeKey").`is`(dedupeKey))
        val update = Update()
            .setOnInsert("dedupeKey", dedupeKey)
            .setOnInsert("recruitId", recruitId)
            .setOnInsert("requiredStep", requiredStep)
            .setOnInsert("target", target)
            .setOnInsert("content", content)
            .setOnInsert("recruitFormId", recruitFormId)
            .setOnInsert("createPreUserBeforeSend", createPreUserBeforeSend)
            .setOnInsert("status", Status.PENDING)
            .setOnInsert("attempts", 0)
            .setOnInsert("stateWaits", 0)
            .setOnInsert("nextAttemptAt", now)
            .setOnInsert("createdAt", now)
            .setOnInsert("updatedAt", now)

        mongoTemplate.upsert(query, update, RecruitSmsOutbox::class.java)
    }

    fun claim(lockTimeout: Duration, now: LocalDateTime = LocalDateTime.now()): ClaimedRecruitSmsOutbox? {
        val lockToken = UUID.randomUUID().toString()
        val claimable = Criteria().orOperator(
            Criteria.where("status").`is`(Status.PENDING).and("nextAttemptAt").lte(now),
            Criteria.where("status").`is`(Status.PROCESSING).and("lockedAt").lte(now.minus(lockTimeout)),
        )
        val query = Query.query(claimable)
            .with(Sort.by(Sort.Direction.ASC, "nextAttemptAt", "createdAt"))
            .limit(1)
        val update = Update()
            .set("status", Status.PROCESSING)
            .set("lockedAt", now)
            .set("lockToken", lockToken)
            .set("updatedAt", now)
            .unset("purgeAt")
            .inc("attempts", 1)

        return mongoTemplate.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            RecruitSmsOutbox::class.java,
        )?.let { ClaimedRecruitSmsOutbox(it, lockToken) }
    }

    fun markProcessed(id: String, lockToken: String, now: LocalDateTime = LocalDateTime.now()) {
        val query = Query.query(
            Criteria.where("_id").`is`(id)
                .and("lockToken").`is`(lockToken)
                .and("status").`is`(Status.PROCESSING),
        )
        val update = Update()
            .set("status", Status.PROCESSED)
            .set("processedAt", now)
            .set("purgeAt", now.plus(terminalRetention))
            .set("updatedAt", now)
            .unset("lockedAt")
            .unset("lockToken")
            .scrubDeliveryPayload()

        mongoTemplate.updateFirst(query, update, RecruitSmsOutbox::class.java)
    }

    fun markFailed(
        id: String,
        lockToken: String,
        attempts: Int,
        maxAttempts: Int,
        nextAttemptAt: LocalDateTime,
        error: String,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        val query = Query.query(
            Criteria.where("_id").`is`(id)
                .and("lockToken").`is`(lockToken)
                .and("status").`is`(Status.PROCESSING),
        )
        val update = Update()
            .set("lastError", error.take(1000))
            .set("updatedAt", now)
            .unset("lockedAt")
            .unset("lockToken")

        if (attempts >= maxAttempts) {
            update.set("status", Status.FAILED)
                .set("failedAt", now)
                .set("purgeAt", now.plus(terminalRetention))
                .scrubDeliveryPayload()
        } else {
            update.set("status", Status.PENDING)
                .set("nextAttemptAt", nextAttemptAt)
                .unset("purgeAt")
        }

        mongoTemplate.updateFirst(query, update, RecruitSmsOutbox::class.java)
    }

    fun deferUntilStateCommitted(
        id: String,
        lockToken: String,
        nextAttemptAt: LocalDateTime,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        val query = processingClaim(id, lockToken)
        val update = Update()
            .set("status", Status.PENDING)
            .set("nextAttemptAt", nextAttemptAt)
            .set("updatedAt", now)
            .unset("lockedAt")
            .unset("lockToken")
            .unset("purgeAt")
            .inc("attempts", -1)
            .inc("stateWaits", 1)

        mongoTemplate.updateFirst(query, update, RecruitSmsOutbox::class.java)
    }

    fun markCanceled(
        id: String,
        lockToken: String,
        reason: String,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        val query = processingClaim(id, lockToken)
        val update = Update()
            .set("status", Status.CANCELED)
            .set("lastError", reason.take(1000))
            .set("failedAt", now)
            .set("purgeAt", now.plus(terminalRetention))
            .set("updatedAt", now)
            .unset("lockedAt")
            .unset("lockToken")
            .scrubDeliveryPayload()

        mongoTemplate.updateFirst(query, update, RecruitSmsOutbox::class.java)
    }

    private fun Update.scrubDeliveryPayload(): Update =
        set("target", "")
            .set("content", "")
            .unset("recruitFormId")

    private fun processingClaim(id: String, lockToken: String): Query =
        Query.query(
            Criteria.where("_id").`is`(id)
                .and("lockToken").`is`(lockToken)
                .and("status").`is`(Status.PROCESSING),
        )
}

data class ClaimedRecruitSmsOutbox(
    val outbox: RecruitSmsOutbox,
    val lockToken: String,
)
