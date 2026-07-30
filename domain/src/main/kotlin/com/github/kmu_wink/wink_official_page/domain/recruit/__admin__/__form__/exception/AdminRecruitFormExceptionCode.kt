package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class AdminRecruitFormExceptionCode(
    private val exceptionMessage: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    NOT_FOUND("모집이 존재하지 않습니다.", 404),
    RECRUITING("모집이 진행 중입니다."),
    PAPER_FAILED_USER("서류에 통과하지 못한 사용자입니다."),
    ALREADY_INTERVIEW_ENDED("이미 면접이 종료되었습니다.", 409),
    ALREADY_PAPER_ENDED("이미 서류가 종료되었습니다.", 409),
    ;

    override fun getMessage(): String = exceptionMessage
    override fun getStatus(): Int = status
}
