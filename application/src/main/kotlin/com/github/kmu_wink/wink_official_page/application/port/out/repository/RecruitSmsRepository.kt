package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit

interface RecruitSmsRepository {
    fun findByRecruit(recruit: Recruit): RecruitSms
    fun save(recruitSms: RecruitSms): RecruitSms
    fun deleteAllByRecruit(recruit: Recruit): Long
}
