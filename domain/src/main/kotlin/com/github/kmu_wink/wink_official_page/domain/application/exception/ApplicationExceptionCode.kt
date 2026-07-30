package com.github.kmu_wink.wink_official_page.domain.application.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class ApplicationExceptionCode(
    private val message: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    NOT_FOUND("애플리케이션을 찾을 수 없습니다.", 404),
    INVALID_SECRET("애플리케이션 시크릿이 올바르지 않습니다.", 401),
    OAUTH_NOT_SUPPORTED("OAuth가 비활성화된 상태입니다."),
    OAUTH_TOKEN_NOT_FOUND("OAuth 토큰을 찾을 수 없습니다.", 401),
    INVALID_IMAGE_URL("본인에게 발급된 애플리케이션 이미지 URL만 사용할 수 있습니다."),
    ;

    override fun getMessage(): String = message
    override fun getStatus(): Int = status
}
