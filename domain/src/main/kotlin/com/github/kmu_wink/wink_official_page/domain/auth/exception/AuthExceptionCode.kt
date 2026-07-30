package com.github.kmu_wink.wink_official_page.domain.auth.exception

import com.github.kmu_wink.wink_official_page.global.exception.ApiExceptionCode

enum class AuthExceptionCode(
    private val message: String,
    private val status: Int = 400,
) : ApiExceptionCode {
    AUTHENTICATION_FAILED("인증에 실패했습니다.", 401),
    ACCESS_TOKEN_EXPIRED("엑세스 토큰이 만료되었습니다.", 401),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", 401),
    INVALID_REGISTER_TOKEN("유효하지 않은 회원가입 토큰입니다.", 401),
    INVALID_PASSWORD_RESET_TOKEN("유효하지 않은 비밀번호 재설정 토큰입니다.", 401),
    ALREADY_REGISTERED("이미 등록된 사용자입니다.", 409),
    TEST_USER_CANNOT_REAL_REGISTER("테스트 사용자는 실제 회원가입을 할 수 없습니다."),
    ;

    override fun getMessage(): String = message
    override fun getStatus(): Int = status
}
