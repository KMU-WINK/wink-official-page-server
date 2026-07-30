package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.global.module.email.EmailTemplate

interface MailPort {
    fun send(email: String, template: EmailTemplate)
}
