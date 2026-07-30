package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import java.util.Optional

interface RecruitFormRepository {
    fun findAllByRecruitOrderByCreatedAtDesc(recruit: Recruit): List<RecruitForm>
    fun findAllByRecruit(recruit: Recruit): List<RecruitForm>
    fun findById(id: String): Optional<RecruitForm>
    fun findByEditTokenDigest(editTokenDigest: String): Optional<RecruitForm>
    fun findByLegacyEditToken(legacyEditToken: String): Optional<RecruitForm>
    fun findByIdAndRecruit(id: String, recruit: Recruit): Optional<RecruitForm>
    fun findByRecruitAndStudentIdBlindIndex(recruit: Recruit, studentIdBlindIndex: String): Optional<RecruitForm>
    fun findByRecruitAndEmailBlindIndex(recruit: Recruit, emailBlindIndex: String): Optional<RecruitForm>
    fun findByRecruitAndPhoneNumberBlindIndex(recruit: Recruit, phoneNumberBlindIndex: String): Optional<RecruitForm>
    fun save(recruitForm: RecruitForm): RecruitForm
    fun deleteAll(recruitForms: Iterable<RecruitForm>)
}
