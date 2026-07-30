package com.github.kmu_wink.wink_official_page.application.port.out

data class SmsMessage(
    val target: String,
    val content: String,
)

interface SmsGatewayPort {
    fun send(messages: Collection<SmsMessage>)

    fun remain(): Int
}
