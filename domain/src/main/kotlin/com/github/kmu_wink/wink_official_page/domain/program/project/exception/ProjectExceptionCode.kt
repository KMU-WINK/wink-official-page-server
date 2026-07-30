package com.github.kmu_wink.wink_official_page.domain.program.project.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class ProjectExceptionCode(
    private val message: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    NOT_FOUND("프로젝트를 찾을 수 없습니다.", 404),
    NOT_OWNER("프로젝트의 소유자가 아닙니다.", 403),
    ;

    override fun getMessage(): String = message
    override fun getStatus(): Int = status
}
