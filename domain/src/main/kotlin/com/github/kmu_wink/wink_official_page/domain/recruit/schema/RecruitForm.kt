package com.github.kmu_wink.wink_official_page.domain.recruit.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.BackendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DesignTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DevOpsTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.FrontendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import java.time.LocalDate
import java.time.LocalDateTime

class RecruitForm(
    var recruit: Recruit? = null,
    var legacyEditToken: String? = null,
    var editTokenDigest: String? = null,
    var editTokenExpiresAt: LocalDateTime? = null,
    var name: String? = null,
    var studentId: String? = null,
    var studentIdBlindIndex: String? = null,
    var department: String? = null,
    var email: String? = null,
    var emailBlindIndex: String? = null,
    var phoneNumber: String? = null,
    var phoneNumberBlindIndex: String? = null,
    var jiwonDonggi: String? = null,
    var selfIntroduce: String? = null,
    var outings: List<String>? = null,
    var interviewDates: List<LocalDate>? = null,
    var whyCannotInterview: String? = null,
    var github: String? = null,
    var frontendTechStacks: List<FrontendTechStack>? = null,
    var backendTechStacks: List<BackendTechStack>? = null,
    var devOpsTechStacks: List<DevOpsTechStack>? = null,
    var designTechStacks: List<DesignTechStack>? = null,
    var favoriteProject: String? = null,
    var paperPass: Boolean? = null,
    var interviewPass: Boolean? = null,
    var privacyPolicyVersion: String? = null,
    var privacyConsentAcceptedAt: LocalDateTime? = null,
    var piiSchemaVersion: Int = RecruitPrivacyPolicy.CURRENT_PII_SCHEMA_VERSION,
) : BaseSchema()
