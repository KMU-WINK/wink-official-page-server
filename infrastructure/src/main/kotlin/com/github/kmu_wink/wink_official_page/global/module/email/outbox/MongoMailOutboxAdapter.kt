package com.github.kmu_wink.wink_official_page.global.module.email.outbox

import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxMessage
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxPort
import org.springframework.stereotype.Component

@Component
class MongoMailOutboxAdapter(
    private val mailOutboxRepository: MailOutboxRepository,
) : MailOutboxPort {
    override fun enqueue(message: MailOutboxMessage) {
        mailOutboxRepository.enqueue(
            dedupeKey = message.dedupeKey,
            recipient = message.recipient,
            title = message.title,
            html = message.html,
            barrierType = message.barrier.type,
            barrierToken = message.barrier.token,
        )
    }
}
