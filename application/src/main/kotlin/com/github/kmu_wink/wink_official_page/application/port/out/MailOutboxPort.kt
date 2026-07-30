package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.global.module.email.EmailTemplate
import java.nio.charset.StandardCharsets
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface MailOutboxPort {
    fun enqueue(message: MailOutboxMessage)
}

data class MailOutboxMessage(
    val dedupeKey: String,
    val recipient: String,
    val title: String,
    val html: String,
    val barrier: MailOutboxBarrier,
) {
    override fun toString(): String =
        "MailOutboxMessage(dedupeKey=$dedupeKey, recipient=[REDACTED], title=[REDACTED], html=[REDACTED], barrier=$barrier)"

    companion object {
        fun of(
            dedupeScope: String,
            recipient: String,
            template: EmailTemplate,
            barrier: MailOutboxBarrier,
        ): MailOutboxMessage =
            MailOutboxMessage(
                dedupeKey = "$dedupeScope:${barrier.dedupeDigest(dedupeScope)}",
                recipient = recipient,
                title = template.getTitle(),
                html = template.getHtml(),
                barrier = barrier,
            )

        private fun MailOutboxBarrier.dedupeDigest(scope: String): String {
            val key = token.toByteArray(StandardCharsets.UTF_8)
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(key, "HmacSHA256"))
            return HexFormat.of().formatHex(
                mac.doFinal("$scope:${type.name}".toByteArray(StandardCharsets.UTF_8)),
            )
        }
    }
}

data class MailOutboxBarrier(
    val type: Type,
    val token: String,
) {
    override fun toString(): String = "MailOutboxBarrier(type=$type, token=[REDACTED])"

    enum class Type {
        PRE_USER_TOKEN,
        RECRUIT_FORM_EDIT_TOKEN,
        PASSWORD_RESET_TOKEN,
    }
}
