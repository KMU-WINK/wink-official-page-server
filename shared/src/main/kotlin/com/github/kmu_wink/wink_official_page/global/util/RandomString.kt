package com.github.kmu_wink.wink_official_page.global.util

import java.security.SecureRandom

object RandomString {
    private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private val secureRandom = SecureRandom()

    fun generate(length: Int): String = buildString(length) {
        repeat(length) {
            append(CHARACTERS[secureRandom.nextInt(CHARACTERS.length)])
        }
    }
}
