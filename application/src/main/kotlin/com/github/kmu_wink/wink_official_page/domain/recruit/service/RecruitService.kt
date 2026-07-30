package com.github.kmu_wink.wink_official_page.domain.recruit.service

import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxBarrier
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxMessage
import com.github.kmu_wink.wink_official_page.application.port.out.MailOutboxPort
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__form__.exception.AdminRecruitFormExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.EmailCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.PhoneNumberCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.RecruitFormRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.request.StudentIdCheckRequest
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.DuplicationCheckResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.GetFormResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.dto.response.GetRecruitResponse
import com.github.kmu_wink.wink_official_page.domain.recruit.email.RecruitFormTemplate
import com.github.kmu_wink.wink_official_page.domain.recruit.exception.RecruitExceptionCode
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import com.github.kmu_wink.wink_official_page.global.crypto.RecruitPiiCryptography
import com.github.kmu_wink.wink_official_page.global.util.RandomString
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class RecruitService(
    private val userRepository: UserRepository,
    private val preUserRepository: PreUserRepository,
    private val recruitRepository: RecruitRepository,
    private val recruitFormRepository: RecruitFormRepository,
    private val mailOutboxPort: MailOutboxPort,
    private val recruitPiiCryptography: RecruitPiiCryptography,
) {
    fun getLatestRecruit(): GetRecruitResponse = GetRecruitResponse(recruitRepository.findLatestRecruit().orElse(null))

    fun saveForm(recruitId: String, dto: RecruitFormRequest) {
        validatePrivacyConsent(dto)
        validateNoCiphertextPrefix(dto)
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }

        validateInterviewDates(dto, recruit)
        validateRecruitPeriod(recruit)

        val studentId = recruitPiiCryptography.normalizeStudentId(dto.studentId)
        val email = recruitPiiCryptography.normalizeEmail(dto.email)
        val phoneNumber = recruitPiiCryptography.normalizePhoneNumber(dto.phoneNumber)
        val studentIdBlindIndex = recruitPiiCryptography.studentIdBlindIndex(studentId)
        val emailBlindIndex = recruitPiiCryptography.emailBlindIndex(email)
        val phoneNumberBlindIndex = recruitPiiCryptography.phoneNumberBlindIndex(phoneNumber)

        if (recruitFormRepository.findByRecruitAndStudentIdBlindIndex(recruit, studentIdBlindIndex).isPresent ||
            recruitFormRepository.findByRecruitAndEmailBlindIndex(recruit, emailBlindIndex).isPresent ||
            recruitFormRepository.findByRecruitAndPhoneNumberBlindIndex(recruit, phoneNumberBlindIndex).isPresent
        ) {
            throw RecruitExceptionCode.ALREADY_RECRUIT_SUBMITTED.toException()
        }

        if (isAlreadyMember(studentId, email, phoneNumber)) {
            throw RecruitExceptionCode.ALREADY_MEMBER.toException()
        }

        val now = LocalDateTime.now()
        val rawEditToken = RandomString.generate(EDIT_TOKEN_LENGTH)
        val editTokenDigest = recruitPiiCryptography.editTokenDigest(rawEditToken)
        val form = RecruitForm(
            recruit = recruit,
            editTokenDigest = editTokenDigest,
            editTokenExpiresAt = editTokenExpiry(recruit),
            name = dto.name.trim(),
            studentId = studentId,
            studentIdBlindIndex = studentIdBlindIndex,
            department = dto.department.trim(),
            email = email,
            emailBlindIndex = emailBlindIndex,
            phoneNumber = phoneNumber,
            phoneNumberBlindIndex = phoneNumberBlindIndex,
            jiwonDonggi = dto.jiwonDonggi.trim(),
            selfIntroduce = dto.selfIntroduce.trim(),
            outings = dto.outings.map(String::trim),
            interviewDates = dto.interviewDates.distinct(),
            whyCannotInterview = minimizedInterviewConstraint(dto),
            github = dto.github?.trim()?.takeIf(String::isNotEmpty),
            frontendTechStacks = dto.frontendTechStacks.distinct(),
            backendTechStacks = dto.backendTechStacks.distinct(),
            devOpsTechStacks = dto.devOpsTechStacks.distinct(),
            designTechStacks = dto.designTechStacks.distinct(),
            favoriteProject = dto.favoriteProject?.trim()?.takeIf(String::isNotEmpty),
            privacyPolicyVersion = RecruitPrivacyPolicy.CURRENT_VERSION,
            privacyConsentAcceptedAt = now,
            piiSchemaVersion = RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION,
        )

        mailOutboxPort.enqueue(
            MailOutboxMessage.of(
                dedupeScope = "recruit-form-submitted",
                recipient = email,
                template = RecruitFormTemplate.of(form, rawEditToken),
                barrier = MailOutboxBarrier(
                    type = MailOutboxBarrier.Type.RECRUIT_FORM_EDIT_TOKEN,
                    token = editTokenDigest,
                ),
            ),
        )

        try {
            recruitFormRepository.save(form)
        } catch (_: DuplicateEntityException) {
            throw RecruitExceptionCode.ALREADY_RECRUIT_SUBMITTED.toException()
        }
    }

    fun getEditForm(editToken: String): GetFormResponse {
        val form = findFormByEditToken(editToken)
        val recruit = form.recruit ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()

        validateRecruitPeriod(recruit)

        return GetFormResponse.of(form)
    }

    fun editForm(editToken: String, dto: RecruitFormRequest) {
        validatePrivacyConsent(dto)
        validateNoCiphertextPrefix(dto)
        val form = findFormByEditToken(editToken)
        val recruit = form.recruit ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()

        validateInterviewDates(dto, recruit)
        validateRecruitPeriod(recruit)

        val studentId = recruitPiiCryptography.normalizeStudentId(dto.studentId)
        val email = recruitPiiCryptography.normalizeEmail(dto.email)
        val phoneNumber = recruitPiiCryptography.normalizePhoneNumber(dto.phoneNumber)
        if (isAlreadyMember(studentId, email, phoneNumber)) {
            throw RecruitExceptionCode.ALREADY_MEMBER.toException()
        }

        form.name = dto.name.trim()
        form.studentId = studentId
        form.studentIdBlindIndex = recruitPiiCryptography.studentIdBlindIndex(studentId)
        form.department = dto.department.trim()
        form.email = email
        form.emailBlindIndex = recruitPiiCryptography.emailBlindIndex(email)
        form.phoneNumber = phoneNumber
        form.phoneNumberBlindIndex = recruitPiiCryptography.phoneNumberBlindIndex(phoneNumber)
        form.jiwonDonggi = dto.jiwonDonggi.trim()
        form.selfIntroduce = dto.selfIntroduce.trim()
        form.outings = dto.outings.map(String::trim)
        form.interviewDates = dto.interviewDates.distinct()
        form.whyCannotInterview = minimizedInterviewConstraint(dto)
        form.github = dto.github?.trim()?.takeIf(String::isNotEmpty)
        form.frontendTechStacks = dto.frontendTechStacks.distinct()
        form.backendTechStacks = dto.backendTechStacks.distinct()
        form.devOpsTechStacks = dto.devOpsTechStacks.distinct()
        form.designTechStacks = dto.designTechStacks.distinct()
        form.favoriteProject = dto.favoriteProject?.trim()?.takeIf(String::isNotEmpty)

        // A successful legacy-token edit opportunistically migrates that document to the digest-only model.
        form.editTokenDigest = recruitPiiCryptography.editTokenDigest(editToken)
        form.editTokenExpiresAt = form.editTokenExpiresAt ?: editTokenExpiry(recruit)
        form.legacyEditToken = null
        if (form.privacyConsentAcceptedAt == null) {
            form.privacyPolicyVersion = RecruitPrivacyPolicy.CURRENT_VERSION
            form.privacyConsentAcceptedAt = LocalDateTime.now()
        }
        form.piiSchemaVersion = RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION

        try {
            recruitFormRepository.save(form)
        } catch (_: DuplicateEntityException) {
            throw RecruitExceptionCode.ALREADY_RECRUIT_SUBMITTED.toException()
        }
    }

    fun checkStudentId(recruitId: String, dto: StudentIdCheckRequest): DuplicationCheckResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val studentId = recruitPiiCryptography.normalizeStudentId(dto.studentId)
        val duplicated = userRepository.findByStudentId(studentId).isPresent ||
            preUserRepository.findByStudentId(studentId).isPresent ||
            recruitFormRepository.findByRecruitAndStudentIdBlindIndex(
                recruit,
                recruitPiiCryptography.studentIdBlindIndex(studentId),
            ).isPresent

        return DuplicationCheckResponse(duplicated)
    }

    fun checkEmail(recruitId: String, dto: EmailCheckRequest): DuplicationCheckResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val email = recruitPiiCryptography.normalizeEmail(dto.email)
        val duplicated = userRepository.findByEmail(email).isPresent ||
            preUserRepository.findByEmail(email).isPresent ||
            recruitFormRepository.findByRecruitAndEmailBlindIndex(
                recruit,
                recruitPiiCryptography.emailBlindIndex(email),
            ).isPresent

        return DuplicationCheckResponse(duplicated)
    }

    fun checkPhoneNumber(recruitId: String, dto: PhoneNumberCheckRequest): DuplicationCheckResponse {
        val recruit = recruitRepository.findById(recruitId)
            .orElseThrow { RecruitExceptionCode.NOT_FOUND.toException() }
        val phoneNumber = recruitPiiCryptography.normalizePhoneNumber(dto.phoneNumber)
        val duplicated = userRepository.findByPhoneNumber(phoneNumber).isPresent ||
            preUserRepository.findByPhoneNumber(phoneNumber).isPresent ||
            recruitFormRepository.findByRecruitAndPhoneNumberBlindIndex(
                recruit,
                recruitPiiCryptography.phoneNumberBlindIndex(phoneNumber),
            ).isPresent

        return DuplicationCheckResponse(duplicated)
    }

    private fun findFormByEditToken(rawEditToken: String): RecruitForm {
        if (rawEditToken.length !in MIN_EDIT_TOKEN_LENGTH..MAX_EDIT_TOKEN_LENGTH) {
            throw AdminRecruitFormExceptionCode.NOT_FOUND.toException()
        }
        val digest = recruitPiiCryptography.editTokenDigest(rawEditToken)
        val form = recruitFormRepository.findByEditTokenDigest(digest)
            .orElseGet {
                recruitFormRepository.findByLegacyEditToken(rawEditToken)
                    .orElseThrow { AdminRecruitFormExceptionCode.NOT_FOUND.toException() }
            }
        val recruit = form.recruit ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        val expiresAt = form.editTokenExpiresAt ?: editTokenExpiry(recruit)
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw AdminRecruitFormExceptionCode.NOT_FOUND.toException()
        }

        return form
    }

    private fun validatePrivacyConsent(dto: RecruitFormRequest) {
        if (!dto.privacyConsent || dto.privacyPolicyVersion != RecruitPrivacyPolicy.CURRENT_VERSION) {
            throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        }
    }

    private fun validateNoCiphertextPrefix(dto: RecruitFormRequest) {
        val userSuppliedText = listOfNotNull(
            dto.name,
            dto.studentId,
            dto.department,
            dto.email,
            dto.phoneNumber,
            dto.jiwonDonggi,
            dto.selfIntroduce,
            dto.whyCannotInterview,
            dto.github,
            dto.favoriteProject,
        ) + dto.outings
        if (userSuppliedText.any { it.startsWith(RecruitPiiCryptography.CURRENT_PREFIX) }) {
            throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        }
    }

    private fun validateInterviewDates(dto: RecruitFormRequest, recruit: Recruit) {
        val start = recruit.interviewStartDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        val end = recruit.interviewEndDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        dto.interviewDates.forEach { date ->
            if (date == LocalDate.of(1, 1, 1)) {
                return@forEach
            }
            if (date.isBefore(start) || date.isAfter(end)) {
                throw RecruitExceptionCode.NOT_VALID_INTERVIEW_DATES.toException()
            }
        }
    }

    private fun minimizedInterviewConstraint(dto: RecruitFormRequest): String? =
        if (UNAVAILABLE_INTERVIEW_DATE in dto.interviewDates) {
            RecruitPrivacyPolicy.INTERVIEW_SCHEDULE_COORDINATION_REQUIRED
        } else {
            null
        }

    private fun validateRecruitPeriod(recruit: Recruit) {
        val start = recruit.recruitStartDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        val end = recruit.recruitEndDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException()
        val now = LocalDateTime.now()
        if (now.isBefore(start.atStartOfDay()) || now.isAfter(end.atTime(LocalTime.MAX))) {
            throw RecruitExceptionCode.NOT_RECRUIT_PERIOD.toException()
        }
    }

    private fun editTokenExpiry(recruit: Recruit): LocalDateTime =
        (recruit.recruitEndDate ?: throw RecruitExceptionCode.INVALID_RECRUIT_FORM.toException())
            .atTime(LocalTime.MAX)

    private fun isAlreadyMember(studentId: String, email: String, phoneNumber: String): Boolean =
        userRepository.findByStudentId(studentId).isPresent ||
            userRepository.findByEmail(email).isPresent ||
            userRepository.findByPhoneNumber(phoneNumber).isPresent ||
            preUserRepository.findByStudentId(studentId).isPresent ||
            preUserRepository.findByEmail(email).isPresent ||
            preUserRepository.findByPhoneNumber(phoneNumber).isPresent

    private companion object {
        const val EDIT_TOKEN_LENGTH = 64
        const val MIN_EDIT_TOKEN_LENGTH = 64
        const val MAX_EDIT_TOKEN_LENGTH = 256
        val UNAVAILABLE_INTERVIEW_DATE: LocalDate = LocalDate.of(1, 1, 1)
    }
}
