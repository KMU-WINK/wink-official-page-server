package com.github.kmu_wink.wink_official_page.global.module.sms

import com.github.kmu_wink.wink_official_page.application.port.out.SmsGatewayPort
import com.github.kmu_wink.wink_official_page.application.port.out.SmsMessage
import com.github.kmu_wink.wink_official_page.global.exception.ApiException
import com.github.kmu_wink.wink_official_page.global.property.SmsProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@Component
class SmsSender(
    private val smsProperty: SmsProperty,
) : SmsGatewayPort {
    private val restClient = RestClient.create("https://www.munja123.com/Remote/")

    override fun send(messages: Collection<SmsMessage>) {
        val body = postForm(
            "RemoteMms.html",
            mapOf(
                "remote_id" to smsProperty.id,
                "remote_pass" to smsProperty.pw,
                "remote_callback" to smsProperty.sendPhone,
                "remote_num" to "1",
                "remote_phone" to messages.joinToString(",") { it.target },
                "remote_msg" to messages.joinToString("__LINE__") { it.content },
            ),
        )

        if (!body.startsWith("0000") && !body.startsWith("0004")) {
            throw ApiException("문자 API 오류: ${body.split("|")[0]}")
        }
    }

    override fun remain(): Int {
        val body = postForm(
            "RemoteCheck.html",
            mapOf(
                "remote_id" to smsProperty.id,
                "remote_pass" to smsProperty.pw,
                "remote_request" to "lms",
            ),
        )

        if (!body.startsWith("0000")) {
            throw ApiException("문자 API 오류: ${body.split("|")[0]}")
        }

        return body.split("|")[2].toInt()
    }

    private fun postForm(path: String, fields: Map<String, String?>): String {
        val form = LinkedMultiValueMap<String, String>()
        fields.forEach { (key, value) -> form.add(key, value.orEmpty()) }

        return restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(String::class.java)
            ?: throw ApiException("문자 API 응답이 비어 있습니다.")
    }
}
