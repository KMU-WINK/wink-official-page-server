package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import java.util.Optional

interface RecruitRepository {
    fun findAllWithSort(): List<Recruit>
    fun findLatestRecruit(): Optional<Recruit>
    fun findById(id: String): Optional<Recruit>
    fun existsRecruitByYearAndSemester(year: Int, semester: Int): Boolean
    fun save(recruit: Recruit): Recruit
    fun delete(recruit: Recruit)
}
