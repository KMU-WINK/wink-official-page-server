package com.github.kmu_wink.wink_official_page.domain.auth.util.email

import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.module.email.EmailTemplate

class PasswordResetTokenTemplate private constructor(
    private val user: User,
    private val token: PasswordResetToken,
) : EmailTemplate {
    override fun getTitle(): String = "[WINK] 비밀번호 재설정"

    override fun getHtml(): String =
        container(
            """
            <h1 class="title">비밀번호 초기화</h1>
            <p class="text">안녕하세요, %s님</p>
            <p class="text">비밀번호를 초기화하려면 아래 버튼을 클릭하세요.</p>
            <p class="text">만약 비밀번호 초기화 요청을 하지 않았다면 이 이메일을 무시해주세요.</p>
            <a href="https://wink.kookmin.ac.kr/auth/reset-password#token=%s" class="button">비밀번호 재설정</a>
            """.trimIndent(),
        ).format(escapeHtml(user.name), token.token)

    companion object {
        fun of(user: User, token: PasswordResetToken) = PasswordResetTokenTemplate(user, token)
    }
}
