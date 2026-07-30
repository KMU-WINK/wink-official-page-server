package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class AdminRecruitExceptionCode(
    private val exceptionMessage: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    ALREADY_EXISTS("이미 해당 학년도, 학기의 모집이 존재합니다.", 409),
    ;

    override fun getMessage(): String = exceptionMessage
    override fun getStatus(): Int = status
}
