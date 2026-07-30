package com.github.kmu_wink.wink_official_page.global.module.email

import com.github.kmu_wink.wink_official_page.application.port.out.MailPort
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailSender(
    private val javaMailSender: JavaMailSender,
) : MailPort {
    @Value("\${spring.mail.username}")
    private lateinit var username: String

    override fun send(email: String, template: EmailTemplate) {
        val message = javaMailSender.createMimeMessage()
        val messageHelper = MimeMessageHelper(message, true, "UTF-8")
        messageHelper.setFrom(InternetAddress(username, ORGANIZATION_NAME))
        messageHelper.setTo(email)
        messageHelper.setSubject(template.getTitle())
        messageHelper.setReplyTo(username)
        messageHelper.setText(transferPlainText(template.getHtml()), template.getHtml())

        message.addHeader("Precedence", "normal")
        message.addHeader("X-Auto-Response-Suppress", "OOF, AutoReply")
        message.addHeader("Message-ID", generateMessageId())

        javaMailSender.send(message)
    }

    private fun transferPlainText(html: String): String = html.replace(Regex("<[^>]*>"), "")

    private fun generateMessageId(): String = "<${RandomString.generate(16)}@${username.split("@")[1]}>"

    private companion object {
        const val ORGANIZATION_NAME = "WINK"
    }
}
