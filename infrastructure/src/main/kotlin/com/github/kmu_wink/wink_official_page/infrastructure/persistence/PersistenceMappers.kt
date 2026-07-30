package com.github.kmu_wink.wink_official_page.infrastructure.persistence

import com.github.kmu_wink.wink_official_page.domain.application.schema.Application
import com.github.kmu_wink.wink_official_page.domain.application.schema.OauthLogin
import com.github.kmu_wink.wink_official_page.domain.auth.schema.PasswordResetToken
import com.github.kmu_wink.wink_official_page.domain.auth.schema.RefreshToken
import com.github.kmu_wink.wink_official_page.domain.program.activity.schema.Activity
import com.github.kmu_wink.wink_official_page.domain.program.history.schema.History
import com.github.kmu_wink.wink_official_page.domain.program.project.schema.Project
import com.github.kmu_wink.wink_official_page.domain.program.study.schema.Study
import com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema.RecruitSms
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.domain.user.schema.User

fun UserDocument.toDomain(): User =
    User(
        email = email,
        name = name,
        studentId = studentId,
        department = department,
        phoneNumber = phoneNumber,
        password = password,
        avatar = avatar,
        description = description,
        social = social,
        role = role,
        fee = fee,
    ).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun User.toDocument(): UserDocument =
    UserDocument(
        email = email,
        name = name,
        studentId = studentId,
        department = department,
        phoneNumber = phoneNumber,
        password = password,
        avatar = avatar,
        description = description,
        social = social,
        role = role,
        fee = fee,
    ).also { it.restoreFrom(this) }

fun PreUserDocument.toDomain(): PreUser =
    PreUser(
        email = email,
        name = name,
        studentId = studentId,
        department = department,
        phoneNumber = phoneNumber,
        token = token,
        test = test,
    ).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun PreUser.toDocument(): PreUserDocument =
    PreUserDocument(
        email = email,
        name = name,
        studentId = studentId,
        department = department,
        phoneNumber = phoneNumber,
        token = token,
        test = test,
    ).also { it.restoreFrom(this) }

fun ApplicationDocument.toDomain(): Application =
    Application(
        baseId = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        img = img,
        secret = secret,
        user = user?.toDomain(),
        login = login,
    )

fun Application.toDocument(): ApplicationDocument =
    ApplicationDocument(name, img, secret, user?.toDocument(), login).also { it.restoreFrom(this) }

fun OauthLoginDocument.toDomain(): OauthLogin = OauthLogin(id, token, clientId, userId, scopes)
fun OauthLogin.toDocument(): OauthLoginDocument = OauthLoginDocument(id, token, clientId, userId, scopes)

fun PasswordResetTokenDocument.toDomain(): PasswordResetToken = PasswordResetToken(id, token, userId)
fun PasswordResetToken.toDocument(): PasswordResetTokenDocument = PasswordResetTokenDocument(id, token, userId)

fun RefreshTokenDocument.toDomain(): RefreshToken = RefreshToken(id, token, userId, ttl)
fun RefreshToken.toDocument(): RefreshTokenDocument = RefreshTokenDocument(id, token, userId, ttl)

fun ActivityDocument.toDomain(): Activity =
    Activity(title, description, images, pinned).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun Activity.toDocument(): ActivityDocument =
    ActivityDocument(title, description, images, pinned).also { it.restoreFrom(this) }

fun HistoryDocument.toDomain(): History =
    History(title, image, date).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun History.toDocument(): HistoryDocument =
    HistoryDocument(title, image, date).also { it.restoreFrom(this) }

fun ProjectDocument.toDomain(): Project =
    Project(author?.toDomain(), title, description, image, link).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun Project.toDocument(): ProjectDocument =
    ProjectDocument(author?.toDocument(), title, description, image, link).also { it.restoreFrom(this) }

fun StudyDocument.toDomain(): Study =
    Study(index, category, title, author, content, image, createdAt, updatedAt).also {
        it.restoreIdentity(id, createdAt, updatedAt)
    }

fun Study.toDocument(): StudyDocument =
    StudyDocument(index, category, title, author, content, image).also { it.restoreFrom(this) }

fun RecruitDocument.toDomain(): Recruit =
    Recruit(year, semester, recruitStartDate, recruitEndDate, interviewStartDate, interviewEndDate, step)
        .also { it.restoreIdentity(id, createdAt, updatedAt) }

fun Recruit.toDocument(): RecruitDocument =
    RecruitDocument(year, semester, recruitStartDate, recruitEndDate, interviewStartDate, interviewEndDate, step)
        .also { it.restoreFrom(this) }

fun RecruitFormDocument.toDomain(): RecruitForm =
    RecruitForm(
        recruit = recruit?.toDomain(),
        legacyEditToken = legacyEditToken,
        editTokenDigest = editTokenDigest,
        editTokenExpiresAt = editTokenExpiresAt,
        name = name,
        studentId = studentId,
        studentIdBlindIndex = studentIdBlindIndex,
        department = department,
        email = email,
        emailBlindIndex = emailBlindIndex,
        phoneNumber = phoneNumber,
        phoneNumberBlindIndex = phoneNumberBlindIndex,
        jiwonDonggi = jiwonDonggi,
        selfIntroduce = selfIntroduce,
        outings = outings,
        interviewDates = interviewDates,
        whyCannotInterview = whyCannotInterview,
        github = github,
        frontendTechStacks = frontendTechStacks,
        backendTechStacks = backendTechStacks,
        devOpsTechStacks = devOpsTechStacks,
        designTechStacks = designTechStacks,
        favoriteProject = favoriteProject,
        paperPass = paperPass,
        interviewPass = interviewPass,
        privacyPolicyVersion = privacyPolicyVersion,
        privacyConsentAcceptedAt = privacyConsentAcceptedAt,
        piiSchemaVersion = piiSchemaVersion,
    ).also { it.restoreIdentity(id, createdAt, updatedAt) }

fun RecruitForm.toDocument(): RecruitFormDocument =
    RecruitFormDocument(
        recruit = recruit?.toDocument(),
        legacyEditToken = legacyEditToken,
        editTokenDigest = editTokenDigest,
        editTokenExpiresAt = editTokenExpiresAt,
        name = name,
        studentId = studentId,
        studentIdBlindIndex = studentIdBlindIndex,
        department = department,
        email = email,
        emailBlindIndex = emailBlindIndex,
        phoneNumber = phoneNumber,
        phoneNumberBlindIndex = phoneNumberBlindIndex,
        jiwonDonggi = jiwonDonggi,
        selfIntroduce = selfIntroduce,
        outings = outings,
        interviewDates = interviewDates,
        whyCannotInterview = whyCannotInterview,
        github = github,
        frontendTechStacks = frontendTechStacks,
        backendTechStacks = backendTechStacks,
        devOpsTechStacks = devOpsTechStacks,
        designTechStacks = designTechStacks,
        favoriteProject = favoriteProject,
        paperPass = paperPass,
        interviewPass = interviewPass,
        privacyPolicyVersion = privacyPolicyVersion,
        privacyConsentAcceptedAt = privacyConsentAcceptedAt,
        piiSchemaVersion = piiSchemaVersion,
    ).also { it.restoreFrom(this) }

fun RecruitSmsDocument.toDomain(): RecruitSms =
    RecruitSms(recruit?.toDomain(), paperFail, paperPass, finalFail, finalPass)
        .also { it.restoreIdentity(id, createdAt, updatedAt) }

fun RecruitSms.toDocument(): RecruitSmsDocument =
    RecruitSmsDocument(recruit?.toDocument(), paperFail, paperPass, finalFail, finalPass)
        .also { it.restoreFrom(this) }

private fun MongoDocumentBase.restoreFrom(domain: com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema) {
    id = domain.id
    createdAt = domain.createdAt
    updatedAt = domain.updatedAt
}
