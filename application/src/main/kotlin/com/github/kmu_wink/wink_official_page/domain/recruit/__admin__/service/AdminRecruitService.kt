package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.service

import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitSmsRepository
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.dto.request.CreateRecruitRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.dto.response.GetRecruitsResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.exception.AdminRecruitExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.GetRecruitResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.exception.RecruitExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import org.springframework.stereotype.Service

@Service
class AdminRecruitService(
    private val recruitRepository: RecruitRepository,
    private val recruitFormRepository: RecruitFormRepository,
    private val recruitSmsRepository: RecruitSmsRepository,
) {
    fun getRecruits(): GetRecruitsResponse = GetRecruitsResponse(recruitRepository.findAllWithSort())

    fun getRecruit(recruitId: String): GetRecruitResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }

        return GetRecruitResponse(recruit)
    }

    fun createRecruit(dto: CreateRecruitRequest): GetRecruitResponse {
        if (recruitRepository.existsRecruitByYearAndSemester(dto.year, dto.semester)) {
            throw AdminRecruitExceptionCode.ALREADY_EXISTS.toException()
        }

        var recruit = Recruit(
            year = dto.year,
            semester = dto.semester,
            recruitStartDate = dto.recruitStartDate,
            recruitEndDate = dto.recruitEndDate,
            interviewStartDate = dto.interviewStartDate,
            interviewEndDate = dto.interviewEndDate,
            step = Recruit.Step.PRE,
        )

        recruit = try {
            recruitRepository.save(recruit)
        } catch (_: DuplicateEntityException) {
            throw AdminRecruitExceptionCode.ALREADY_EXISTS.toException()
        }

        try {
            recruitSmsRepository.save(RecruitSms(recruit = recruit))
        } catch (exception: RuntimeException) {
            runCatching { recruitRepository.delete(recruit) }
                .onFailure(exception::addSuppressed)
            throw exception
        }

        return GetRecruitResponse(recruit)
    }

    fun deleteRecruit(recruitId: String) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        recruitSmsRepository.deleteAllByRecruit(recruit)
        recruitFormRepository.deleteAll(recruitFormRepository.findAllByRecruit(recruit))
        recruitRepository.delete(recruit)
    }
}
