package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.service

import com.github.kmu_wink.wink_official_page.application.port.out.SmsGatewayPort
import com.github.kmu_wink.wink_official_page.application.port.out.SmsMessage
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitSmsRepository
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.constant.TestSmsField
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.dto.request.SendTestSmsRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.dto.request.UpdateRecruitSmsRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.dto.response.GetRecruitSmsResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.exception.AdminRecruitSmsExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.exception.RecruitExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.stereotype.Service

@Service
class AdminRecruitSmsService(
    private val recruitRepository: RecruitRepository,
    private val recruitSmsRepository: RecruitSmsRepository,
    private val preUserRepository: PreUserRepository,
    private val smsGatewayPort: SmsGatewayPort,
) {
    fun getRecruitSms(recruitId: String): GetRecruitSmsResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val sms = recruitSmsRepository.findByRecruit(recruit)

        return GetRecruitSmsResponse(sms)
    }

    fun updateRecruitSms(recruitId: String, dto: UpdateRecruitSmsRequest): GetRecruitSmsResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        var sms = recruitSmsRepository.findByRecruit(recruit)

        sms.paperFail = dto.paperFail
        sms.paperPass = dto.paperPass
        sms.finalFail = dto.finalFail
        sms.finalPass = dto.finalPass
        sms = recruitSmsRepository.save(sms)

        return GetRecruitSmsResponse(sms)
    }

    fun sendTestSms(recruitId: String, dto: SendTestSmsRequest, user: User) {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val sms = recruitSmsRepository.findByRecruit(recruit)

        if (smsGatewayPort.remain() < 1) {
            throw AdminRecruitSmsExceptionCode.LACK_SMS_CREDIT.toException()
        }

        if (sms.paperFail == null || sms.paperPass == null) {
            throw AdminRecruitSmsExceptionCode.MESSAGE_IS_EMPTY.toException()
        }

        val content = when (dto.field) {
            TestSmsField.PAPER_FAIL -> sms.paperFail
            TestSmsField.PAPER_PASS -> sms.paperPass
            TestSmsField.FINAL_FAIL -> sms.finalFail
            TestSmsField.FINAL_PASS -> sms.finalPass
            null -> throw AdminRecruitSmsExceptionCode.MESSAGE_IS_EMPTY.toException()
        } ?: throw AdminRecruitSmsExceptionCode.MESSAGE_IS_EMPTY.toException()

        val transform = if (dto.field == TestSmsField.FINAL_PASS) {
            RecruitSms.transform(
                content,
                getOrCreateTestPreUser(user),
            )
        } else {
            RecruitSms.transform(
                content,
                RecruitForm(
                    name = user.name,
                    studentId = user.studentId,
                    department = user.department,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                ),
            )
        }

        smsGatewayPort.send(listOf(SmsMessage(dto.phoneNumber, transform)))
    }

    private fun getOrCreateTestPreUser(user: User): PreUser {
        val studentId = requireNotNull(user.studentId)
        val preUser = preUserRepository.findByTestTrueAndStudentId(studentId).orElseGet {
            PreUser(
                studentId = studentId,
                token = RandomString.generate(128),
                test = true,
            )
        }

        preUser.name = "${user.name} (테스트)"
        preUser.department = user.department
        preUser.email = user.email
        preUser.phoneNumber = user.phoneNumber
        return preUserRepository.save(preUser)
    }
}
