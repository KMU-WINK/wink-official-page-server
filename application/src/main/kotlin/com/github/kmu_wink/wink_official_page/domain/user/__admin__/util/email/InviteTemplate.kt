package com.github.kmu_wink.wink_official_page.domain.user.__admin__.util.email

import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.global.module.email.EmailTemplate

class InviteTemplate private constructor(
    private val preUser: PreUser,
) : EmailTemplate {
    override fun getTitle(): String = "[WINK] 부원 초대"

    override fun getHtml(): String =
        container(
            """
            <h1 class="title">부원 초대</h1>
            <p class="text">안녕하세요, %s님</p>
            <p class="text">WINK에 가입하기 위해, 아래 버튼을 눌러주세요.</p>
            <a href="https://wink.kookmin.ac.kr/auth/register#token=%s" class="button">가입하기</a>
            """.trimIndent(),
        ).format(escapeHtml(preUser.name), preUser.token)

    companion object {
        fun of(preUser: PreUser) = InviteTemplate(preUser)
    }
}
