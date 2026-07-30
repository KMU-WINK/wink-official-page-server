package com.github.kmu_wink.wink_official_page.domain.recruit.outbox

import org.springframework.stereotype.Component

@Component
class MongoRecruitSmsOutboxAdapter(
    private val recruitSmsOutboxRepository: RecruitSmsOutboxRepository,
) : RecruitSmsOutboxPort {
    override fun enqueue(messages: Collection<RecruitSmsOutboxMessage>) {
        messages.forEach { message ->
            recruitSmsOutboxRepository.enqueue(
                dedupeKey = message.dedupeKey,
                recruitId = message.recruitId,
                requiredStep = message.requiredStep,
                target = message.target,
                content = message.content,
                recruitFormId = message.recruitFormId,
                createPreUserBeforeSend = message.createPreUserBeforeSend,
            )
        }
    }
}
