package com.github.kmu_wink.wink_official_page.domain.recruit.outbox

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit

interface RecruitSmsOutboxPort {
    fun enqueue(messages: Collection<RecruitSmsOutboxMessage>)
}

data class RecruitSmsOutboxMessage(
    val dedupeKey: String,
    val recruitId: String,
    val requiredStep: Recruit.Step,
    val target: String,
    val content: String,
    val recruitFormId: String? = null,
    val createPreUserBeforeSend: Boolean = false,
) {
    override fun toString(): String =
        "RecruitSmsOutboxMessage(" +
            "dedupeKey=$dedupeKey, " +
            "recruitId=$recruitId, " +
            "requiredStep=$requiredStep, " +
            "target=[REDACTED], " +
            "content=[REDACTED], " +
            "recruitFormId=$recruitFormId, " +
            "createPreUserBeforeSend=$createPreUserBeforeSend" +
            ")"
}
