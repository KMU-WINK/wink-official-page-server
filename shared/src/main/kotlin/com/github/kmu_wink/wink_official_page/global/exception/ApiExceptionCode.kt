package com.github.kmu_wink.wink_official_page.global.exception

interface ApiExceptionCode {
    fun getMessage(): String

    fun getStatus(): Int = 400

    fun toException(): ApiException = ApiException(this)
}
