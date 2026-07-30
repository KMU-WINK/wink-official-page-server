package com.github.kmu_wink.wink_official_page.global.util

object RegExp {
    const val NAME_EXPRESSION = "^[가-힣]{2,5}$"
    const val NAME_MESSAGE = "올바른 이름이 아닙니다."

    const val PASSWORD_EXPRESSION = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$"
    const val PASSWORD_MESSAGE = "비밀번호는 8자 이상의 영문자 및 숫자 조합으로 작성해주세요."

    const val KOOKMIN_EMAIL_EXPRESSION = "^[a-zA-Z0-9._%+-]+@kookmin\\.ac\\.kr$"
    const val KOOKMIN_EMAIL_MESSAGE = "국민대학교 이메일 형식이 아닙니다."

    const val GITHUB_USERNAME_EXPRESSION = "^(?!-)[a-zA-Z0-9-]{1,39}(?<!-)$"
    const val GITHUB_USERNAME_MESSAGE = "올바른 Github 유저가 아닙니다."

    const val GITHUB_PROJECT_URL_EXPRESSION =
        "^https?:\\/\\/(?:www\\.)?github\\.com\\/[a-zA-Z0-9-]+(?:\\/[a-zA-Z0-9._-]+)?\\/?$"
    const val GITHUB_PROJECT_URL_MESSAGE = "올바른 Github URL이 아닙니다."

    const val INSTAGRAM_EXPRESSION = "^(?!.*\\.\\.)(?!.*\\.$)[a-zA-Z0-9._]{1,30}$"
    const val INSTAGRAM_MESSAGE = "올바른 Instagram 유저가 아닙니다."

    const val URL_EXPRESSION = "^https?:\\/\\/(localhost|www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}" +
        ".[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$|^https?:\\/\\/localhost(:\\d+)?(\\/.*)?$"
    const val URL_MESSAGE = "올바른 URL이 아닙니다."

    const val STUDENT_ID_MESSAGE = "올바른 학번이 아닙니다."

    const val PHONE_NUMBER_EXPRESSION = "^(01[016789])-[0-9]{3,4}-[0-9]{4}$"
    const val PHONE_NUMBER_MESSAGE = "올바른 전화번호가 아닙니다."
}
