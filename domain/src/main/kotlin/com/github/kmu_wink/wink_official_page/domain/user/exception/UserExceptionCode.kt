package com.github.kmu_wink.wink_official_page.domain.user.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class UserExceptionCode(
    private val message: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    NOT_FOUND("사용자를 찾을 수 없습니다.", 404),
    INVALID_AVATAR_URL("본인에게 발급된 프로필 이미지 URL만 사용할 수 있습니다."),
    ;

    override fun getMessage(): String = message
    override fun getStatus(): Int = status
}
