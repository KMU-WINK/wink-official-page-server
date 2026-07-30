package com.github.kmu_wink.wink_official_page.domain.recruit.privacy

import com.github.kmu_wink.wink_official_page.infrastructure.persistence.RecruitDocument
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.RecruitFormDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
@ConditionalOnProperty(
    prefix = "app.recruit.privacy.retention",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class RecruitFormRetentionWorker(
    private val mongoTemplate: MongoTemplate,
    @Value("\${app.recruit.privacy.retention.retention-days:90}")
    retentionDays: Long,
    @Value("\${app.recruit.privacy.retention.zone:Asia/Seoul}")
    zone: String,
) {
    private val retentionDays = retentionDays.also {
        require(it > 0) { "Recruit form retention days must be positive" }
    }
    private val zoneId = ZoneId.of(zone)

    @Scheduled(
        cron = "\${app.recruit.privacy.retention.cron:0 30 3 * * *}",
        zone = "\${app.recruit.privacy.retention.zone:Asia/Seoul}",
    )
    fun purgeExpiredForms() {
        val today = LocalDate.now(zoneId)
        var deletedCount = 0L

        mongoTemplate.findAll(RecruitDocument::class.java)
            .asSequence()
            .filter { recruit -> isPastRetentionPeriod(recruit, today) }
            .forEach { recruit ->
                val query = Query.query(Criteria.where("recruit").`is`(recruit))
                deletedCount += mongoTemplate.remove(query, RecruitFormDocument::class.java).deletedCount
            }

        log.info(
            "Recruit form retention completed; deletedCount={}, retentionDays={}",
            deletedCount,
            retentionDays,
        )
    }

    private fun isPastRetentionPeriod(recruit: RecruitDocument, today: LocalDate): Boolean {
        val processEndDate = listOfNotNull(recruit.recruitEndDate, recruit.interviewEndDate).maxOrNull()
            ?: return false
        return processEndDate.plusDays(retentionDays).isBefore(today)
    }

    private companion object {
        val log = LoggerFactory.getLogger(RecruitFormRetentionWorker::class.java)
    }
}
