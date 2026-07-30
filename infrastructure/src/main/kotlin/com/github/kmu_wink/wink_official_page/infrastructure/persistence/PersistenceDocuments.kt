package com.github.kmu_wink.wink_official_page.infrastructure.persistence

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.BackendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DesignTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.DevOpsTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.FrontendTechStack
import com.github.kmu_wink.wink_official_page.domain.recruit.constant.RecruitPrivacyPolicy
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

abstract class MongoDocumentBase {
    @Id
    var id: String? = null

    @CreatedDate
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}

@Document(collection = "application")
class ApplicationDocument(
    var name: String? = null,
    var img: String? = null,
    var secret: String? = null,
    @DBRef
    var user: UserDocument? = null,
    var login: Application.Login = Application.Login(),
) : MongoDocumentBase()

@RedisHash(value = "oauth_login", timeToLive = 60 * 15)
data class OauthLoginDocument(
    @Id
    var id: Long? = null,
    @Indexed
    var token: String? = null,
    var clientId: String? = null,
    var userId: String? = null,
    var scopes: List<Application.Login.Scope> = emptyList(),
)

@RedisHash(value = "password_reset_token", timeToLive = 60 * 60)
data class PasswordResetTokenDocument(
    @Id
    var id: Long? = null,
    @Indexed
    var token: String? = null,
    var userId: String? = null,
)

@RedisHash(value = "refresh_token")
data class RefreshTokenDocument(
    @Id
    var id: Long? = null,
    @Indexed
    var token: String? = null,
    var userId: String? = null,
    @TimeToLive(unit = TimeUnit.HOURS)
    var ttl: Long = 0,
)

@Document(collection = "activity")
class ActivityDocument(
    var title: String = "",
    var description: String = "",
    var images: List<String> = emptyList(),
    var pinned: Boolean = false,
) : MongoDocumentBase()

@Document(collection = "history")
class HistoryDocument(
    var title: String = "",
    var image: String? = null,
    var date: LocalDate? = null,
) : MongoDocumentBase()

@Document(collection = "project")
class ProjectDocument(
    @DBRef
    var author: UserDocument? = null,
    var title: String = "",
    var description: String = "",
    var image: String? = null,
    var link: String = "",
) : MongoDocumentBase()

@Document(collection = "study")
class StudyDocument(
    @Indexed(unique = true)
    var index: Int = 0,
    var category: String = "",
    var title: String = "",
    var author: String = "",
    var content: String = "",
    var image: String? = null,
) : MongoDocumentBase()

@Document(collection = "recruit")
@CompoundIndex(
    name = "recruit_year_semester_unique",
    def = "{'year': 1, 'semester': 1}",
    unique = true,
)
class RecruitDocument(
    var year: Int = 0,
    var semester: Int = 0,
    var recruitStartDate: LocalDate? = null,
    var recruitEndDate: LocalDate? = null,
    var interviewStartDate: LocalDate? = null,
    var interviewEndDate: LocalDate? = null,
    var step: Recruit.Step? = null,
) : MongoDocumentBase()

@Document(collection = "recruitForm")
@CompoundIndexes(
    CompoundIndex(
        name = "uq_recruit_form_recruit_student_id_blind_index",
        def = "{'recruit.\$id': 1, 'studentIdBlindIndex': 1}",
        unique = true,
        partialFilter = "{'studentIdBlindIndex': {'\$type': 'string'}}",
    ),
    CompoundIndex(
        name = "uq_recruit_form_recruit_email_blind_index",
        def = "{'recruit.\$id': 1, 'emailBlindIndex': 1}",
        unique = true,
        partialFilter = "{'emailBlindIndex': {'\$type': 'string'}}",
    ),
    CompoundIndex(
        name = "uq_recruit_form_recruit_phone_number_blind_index",
        def = "{'recruit.\$id': 1, 'phoneNumberBlindIndex': 1}",
        unique = true,
        partialFilter = "{'phoneNumberBlindIndex': {'\$type': 'string'}}",
    ),
)
class RecruitFormDocument(
    @DBRef
    var recruit: RecruitDocument? = null,
    @Field("editToken")
    var legacyEditToken: String? = null,
    @Indexed(
        unique = true,
        partialFilter = "{'editTokenDigest': {'\$type': 'string'}}",
    )
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
) : MongoDocumentBase()

@Document(collection = "recruitSms")
class RecruitSmsDocument(
    @DBRef
    @Indexed(name = "recruit_sms_recruit_unique", unique = true)
    var recruit: RecruitDocument? = null,
    var paperFail: String? = null,
    var paperPass: String? = null,
    var finalFail: String? = null,
    var finalPass: String? = null,
) : MongoDocumentBase()

abstract class BaseUserDocument(
    @Indexed(unique = true, sparse = true)
    var email: String? = null,
    var name: String? = null,
    @Indexed(unique = true, sparse = true)
    var studentId: String? = null,
    var department: String? = null,
    @Indexed(unique = true, sparse = true)
    var phoneNumber: String? = null,
) : MongoDocumentBase()

@Document(collection = "preUser")
class PreUserDocument(
    email: String? = null,
    name: String? = null,
    studentId: String? = null,
    department: String? = null,
    phoneNumber: String? = null,
    @Indexed(unique = true, sparse = true)
    var token: String? = null,
    var test: Boolean = false,
) : BaseUserDocument(email, name, studentId, department, phoneNumber)

@Document(collection = "user")
class UserDocument(
    email: String? = null,
    name: String? = null,
    studentId: String? = null,
    department: String? = null,
    phoneNumber: String? = null,
    var password: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var social: User.Social? = null,
    var role: User.Role? = null,
    var fee: Boolean = false,
) : BaseUserDocument(email, name, studentId, department, phoneNumber)
