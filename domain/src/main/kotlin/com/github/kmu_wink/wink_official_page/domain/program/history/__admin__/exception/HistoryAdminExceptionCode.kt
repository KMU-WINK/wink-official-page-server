package com.github.kmu_wink.wink_official_page.domain.program.history.__admin__.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class HistoryAdminExceptionCode(
    private val message: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    NOT_FOUND("연혁을 찾을 수 없습니다.", 404),
    ;

    override fun getMessage(): String = message
    override fun getStatus(): Int = status
}
