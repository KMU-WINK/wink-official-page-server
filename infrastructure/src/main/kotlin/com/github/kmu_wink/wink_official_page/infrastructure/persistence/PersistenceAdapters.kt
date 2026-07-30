package com.github.kmu_wink.wink_official_page.infrastructure.persistence

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.application.port.out.DuplicateEntityException
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ActivityRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ApplicationRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.HistoryRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.OauthLoginRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PasswordResetTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.PreUserRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.ProjectRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitFormRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RecruitSmsRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.RefreshTokenRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.StudyRepository
import com.github.kmu_wink.wink_official_page.application.port.out.repository.UserRepository
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SpringApplicationDocumentRepository : MongoRepository<ApplicationDocument, String> {
    fun findAllByUser(user: UserDocument): List<ApplicationDocument>
}

@Repository
interface SpringOauthLoginDocumentRepository : KeyValueRepository<OauthLoginDocument, Long> {
    fun findByToken(token: String): Optional<OauthLoginDocument>
}

@Repository
interface SpringPasswordResetTokenDocumentRepository : KeyValueRepository<PasswordResetTokenDocument, Long> {
    fun findByToken(token: String): Optional<PasswordResetTokenDocument>
}

@Repository
interface SpringRefreshTokenDocumentRepository : KeyValueRepository<RefreshTokenDocument, Long> {
    fun findByToken(token: String): Optional<RefreshTokenDocument>
}

@Repository
interface SpringUserDocumentRepository : MongoRepository<UserDocument, String> {
    fun findByStudentId(studentId: String): Optional<UserDocument>
    fun findByEmail(email: String): Optional<UserDocument>
    fun findByPhoneNumber(phoneNumber: String): Optional<UserDocument>
}

@Repository
interface SpringPreUserDocumentRepository : MongoRepository<PreUserDocument, String> {
    @Query(
        "{ \$or: [ {'name': { \$regex: ?0, \$options: 'i' }}, {'studentId': { \$regex: ?0, \$options: 'i' }}, " +
            "{'email': { \$regex: ?0, \$options: 'i' }}, {'phoneNumber': { \$regex: ?0, \$options: 'i' }} ]}",
    )
    fun findAllSearch(query: String, pageable: org.springframework.data.domain.Pageable): Page<PreUserDocument>
    fun findByToken(token: String): Optional<PreUserDocument>
    fun findByTestTrueAndStudentId(studentId: String): Optional<PreUserDocument>
    fun findByStudentId(studentId: String): Optional<PreUserDocument>
    fun findByEmail(email: String): Optional<PreUserDocument>
    fun findByPhoneNumber(phoneNumber: String): Optional<PreUserDocument>
}

@Repository
interface SpringActivityDocumentRepository : MongoRepository<ActivityDocument, String> {
    @Query("{ \$or: [ {'title': { \$regex: ?0 }}, {'description': { \$regex: ?0 }} ]}")
    fun findAllSearch(query: String, pageable: org.springframework.data.domain.Pageable): Page<ActivityDocument>

    @Query("{ 'pinned': true }")
    fun findAllPinned(): List<ActivityDocument>
}

@Repository
interface SpringHistoryDocumentRepository : MongoRepository<HistoryDocument, String> {
    @Query(value = "{}", sort = "{ date: -1 }")
    fun findAllWithSort(): List<HistoryDocument>
}

@Repository
interface SpringProjectDocumentRepository : MongoRepository<ProjectDocument, String> {
    @Query(value = "{ \$or: [ {'title': { \$regex: ?0 }} ]}", fields = "{ 'content': 0 }")
    fun findAllSearch(query: String, pageable: org.springframework.data.domain.Pageable): Page<ProjectDocument>
}

@Repository
interface SpringStudyDocumentRepository : MongoRepository<StudyDocument, String> {
    @Query("{ \$or: [ {'title': { \$regex: ?0 }}, {'author': { \$regex: ?0 }} ]}")
    fun findAllSearch(query: String, pageable: org.springframework.data.domain.Pageable): Page<StudyDocument>

    @Query("{ 'category': ?0, \$or: [ {'title': { \$regex: ?1 }}, {'author': { \$regex: ?1 }} ]}")
    fun findAllByCategoryAndSearch(
        category: String,
        query: String,
        pageable: org.springframework.data.domain.Pageable,
    ): Page<StudyDocument>

    fun findTopByOrderByIndexDesc(): Optional<StudyDocument>
}

@Repository
interface SpringRecruitDocumentRepository : MongoRepository<RecruitDocument, String> {
    @Query(value = "{}", sort = "{ year: -1, semester: -1 }")
    fun findAllWithSort(): List<RecruitDocument>

    @Aggregation(pipeline = ["{ \$sort: { year: -1, semester: -1 } }", "{ \$limit: 1 }"])
    fun findLatestRecruit(): Optional<RecruitDocument>

    fun existsRecruitByYearAndSemester(year: Int, semester: Int): Boolean
}

@Repository
interface SpringRecruitFormDocumentRepository : MongoRepository<RecruitFormDocument, String> {
    fun findAllByRecruitOrderByCreatedAtDesc(recruit: RecruitDocument): List<RecruitFormDocument>
    fun findAllByRecruit(recruit: RecruitDocument): List<RecruitFormDocument>
    fun findByEditTokenDigest(editTokenDigest: String): Optional<RecruitFormDocument>
    fun findByLegacyEditToken(legacyEditToken: String): Optional<RecruitFormDocument>
    fun findByIdAndRecruit(id: String, recruit: RecruitDocument): Optional<RecruitFormDocument>
    fun findByRecruitAndStudentIdBlindIndex(
        recruit: RecruitDocument,
        studentIdBlindIndex: String,
    ): Optional<RecruitFormDocument>
    fun findByRecruitAndEmailBlindIndex(recruit: RecruitDocument, emailBlindIndex: String): Optional<RecruitFormDocument>
    fun findByRecruitAndPhoneNumberBlindIndex(
        recruit: RecruitDocument,
        phoneNumberBlindIndex: String,
    ): Optional<RecruitFormDocument>
}

@Repository
interface SpringRecruitSmsDocumentRepository : MongoRepository<RecruitSmsDocument, String> {
    fun findByRecruit(recruit: RecruitDocument): RecruitSmsDocument
    fun deleteAllByRecruit(recruit: RecruitDocument): Long
}

@Component
class MongoApplicationRepositoryAdapter(
    private val repository: SpringApplicationDocumentRepository,
) : ApplicationRepository {
    override fun findAllByUser(user: User): List<Application> =
        repository.findAllByUser(user.toDocument()).map { it.toDomain() }

    override fun findById(id: String): Optional<Application> = repository.findById(id).map { it.toDomain() }
    override fun save(application: Application): Application = saveOrDuplicate { repository.save(application.toDocument()) }.toDomain()
    override fun delete(application: Application) = repository.delete(application.toDocument())
}

@Component
class RedisOauthLoginRepositoryAdapter(
    private val repository: SpringOauthLoginDocumentRepository,
) : OauthLoginRepository {
    override fun findByToken(token: String): Optional<OauthLogin> = repository.findByToken(token).map { it.toDomain() }
    override fun save(oauthLogin: OauthLogin): OauthLogin = saveOrDuplicate { repository.save(oauthLogin.toDocument()) }.toDomain()
    override fun delete(oauthLogin: OauthLogin) = repository.delete(oauthLogin.toDocument())
}

@Component
class RedisPasswordResetTokenRepositoryAdapter(
    private val repository: SpringPasswordResetTokenDocumentRepository,
) : PasswordResetTokenRepository {
    override fun findByToken(token: String): Optional<PasswordResetToken> =
        repository.findByToken(token).map { it.toDomain() }

    override fun save(passwordResetToken: PasswordResetToken): PasswordResetToken =
        saveOrDuplicate { repository.save(passwordResetToken.toDocument()) }.toDomain()

    override fun delete(passwordResetToken: PasswordResetToken) = repository.delete(passwordResetToken.toDocument())
}

@Component
class RedisRefreshTokenRepositoryAdapter(
    private val repository: SpringRefreshTokenDocumentRepository,
) : RefreshTokenRepository {
    override fun findByToken(token: String): Optional<RefreshToken> = repository.findByToken(token).map { it.toDomain() }
    override fun save(refreshToken: RefreshToken): RefreshToken = saveOrDuplicate { repository.save(refreshToken.toDocument()) }.toDomain()
    override fun delete(refreshToken: RefreshToken) = repository.delete(refreshToken.toDocument())
}

@Component
class MongoUserRepositoryAdapter(
    private val repository: SpringUserDocumentRepository,
) : UserRepository {
    override fun findById(id: String): Optional<User> = repository.findById(id).map { it.toDomain() }
    override fun findByStudentId(studentId: String): Optional<User> = repository.findByStudentId(studentId).map { it.toDomain() }
    override fun findByEmail(email: String): Optional<User> = repository.findByEmail(email).map { it.toDomain() }
    override fun findByPhoneNumber(phoneNumber: String): Optional<User> = repository.findByPhoneNumber(phoneNumber).map { it.toDomain() }
    override fun save(user: User): User = saveOrDuplicate { repository.save(user.toDocument()) }.toDomain()
}

@Component
class MongoPreUserRepositoryAdapter(
    private val repository: SpringPreUserDocumentRepository,
) : PreUserRepository {
    override fun findById(id: String): Optional<PreUser> = repository.findById(id).map { it.toDomain() }
    override fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<PreUser> =
        repository.findAllSearch(query, pageQuery.toPageable()).mapResult { it.toDomain() }

    override fun findByToken(token: String): Optional<PreUser> = repository.findByToken(token).map { it.toDomain() }
    override fun findByTestTrueAndStudentId(studentId: String): Optional<PreUser> =
        repository.findByTestTrueAndStudentId(studentId).map { it.toDomain() }

    override fun findByStudentId(studentId: String): Optional<PreUser> = repository.findByStudentId(studentId).map { it.toDomain() }
    override fun findByEmail(email: String): Optional<PreUser> = repository.findByEmail(email).map { it.toDomain() }
    override fun findByPhoneNumber(phoneNumber: String): Optional<PreUser> = repository.findByPhoneNumber(phoneNumber).map { it.toDomain() }
    override fun save(preUser: PreUser): PreUser = saveOrDuplicate { repository.save(preUser.toDocument()) }.toDomain()
    override fun delete(preUser: PreUser) = repository.delete(preUser.toDocument())
}

@Component
class MongoActivityRepositoryAdapter(
    private val repository: SpringActivityDocumentRepository,
) : ActivityRepository {
    override fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Activity> =
        repository.findAllSearch(query, pageQuery.toPageable()).mapResult { it.toDomain() }

    override fun findAllPinned(): List<Activity> = repository.findAllPinned().map { it.toDomain() }
    override fun findById(id: String): Optional<Activity> = repository.findById(id).map { it.toDomain() }
    override fun save(activity: Activity): Activity = saveOrDuplicate { repository.save(activity.toDocument()) }.toDomain()
    override fun delete(activity: Activity) = repository.delete(activity.toDocument())
}

@Component
class MongoHistoryRepositoryAdapter(
    private val repository: SpringHistoryDocumentRepository,
) : HistoryRepository {
    override fun findAllWithSort(): List<History> = repository.findAllWithSort().map { it.toDomain() }
    override fun findById(id: String): Optional<History> = repository.findById(id).map { it.toDomain() }
    override fun save(history: History): History = saveOrDuplicate { repository.save(history.toDocument()) }.toDomain()
    override fun delete(history: History) = repository.delete(history.toDocument())
}

@Component
class MongoProjectRepositoryAdapter(
    private val repository: SpringProjectDocumentRepository,
) : ProjectRepository {
    override fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Project> =
        repository.findAllSearch(query, pageQuery.toPageable()).mapResult { it.toDomain() }

    override fun findById(id: String): Optional<Project> = repository.findById(id).map { it.toDomain() }
    override fun save(project: Project): Project = saveOrDuplicate { repository.save(project.toDocument()) }.toDomain()
    override fun delete(project: Project) = repository.delete(project.toDocument())
}

@Component
class MongoStudyRepositoryAdapter(
    private val repository: SpringStudyDocumentRepository,
) : StudyRepository {
    override fun findAll(): List<Study> = repository.findAll().map { it.toDomain() }
    override fun findAllSearch(query: String, pageQuery: PageQuery): PageResult<Study> =
        repository.findAllSearch(query, pageQuery.toPageable()).mapResult { it.toDomain() }

    override fun findAllByCategoryAndSearch(category: String, query: String, pageQuery: PageQuery): PageResult<Study> =
        repository.findAllByCategoryAndSearch(category, query, pageQuery.toPageable()).mapResult { it.toDomain() }

    override fun findTopByOrderByIndexDesc(): Optional<Study> =
        repository.findTopByOrderByIndexDesc().map { it.toDomain() }

    override fun save(study: Study): Study = saveOrDuplicate { repository.save(study.toDocument()) }.toDomain()
}

@Component
class MongoRecruitRepositoryAdapter(
    private val repository: SpringRecruitDocumentRepository,
) : RecruitRepository {
    override fun findAllWithSort(): List<Recruit> = repository.findAllWithSort().map { it.toDomain() }
    override fun findLatestRecruit(): Optional<Recruit> = repository.findLatestRecruit().map { it.toDomain() }
    override fun findById(id: String): Optional<Recruit> = repository.findById(id).map { it.toDomain() }
    override fun existsRecruitByYearAndSemester(year: Int, semester: Int): Boolean =
        repository.existsRecruitByYearAndSemester(year, semester)

    override fun save(recruit: Recruit): Recruit = saveOrDuplicate { repository.save(recruit.toDocument()) }.toDomain()
    override fun delete(recruit: Recruit) = repository.delete(recruit.toDocument())
}

@Component
class MongoRecruitFormRepositoryAdapter(
    private val repository: SpringRecruitFormDocumentRepository,
) : RecruitFormRepository {
    override fun findAllByRecruitOrderByCreatedAtDesc(recruit: Recruit): List<RecruitForm> =
        repository.findAllByRecruitOrderByCreatedAtDesc(recruit.toDocument()).map { it.toDomain() }

    override fun findAllByRecruit(recruit: Recruit): List<RecruitForm> =
        repository.findAllByRecruit(recruit.toDocument()).map { it.toDomain() }

    override fun findById(id: String): Optional<RecruitForm> = repository.findById(id).map { it.toDomain() }
    override fun findByEditTokenDigest(editTokenDigest: String): Optional<RecruitForm> =
        repository.findByEditTokenDigest(editTokenDigest).map { it.toDomain() }

    override fun findByLegacyEditToken(legacyEditToken: String): Optional<RecruitForm> =
        repository.findByLegacyEditToken(legacyEditToken).map { it.toDomain() }

    override fun findByIdAndRecruit(id: String, recruit: Recruit): Optional<RecruitForm> =
        repository.findByIdAndRecruit(id, recruit.toDocument()).map { it.toDomain() }

    override fun findByRecruitAndStudentIdBlindIndex(
        recruit: Recruit,
        studentIdBlindIndex: String,
    ): Optional<RecruitForm> =
        repository.findByRecruitAndStudentIdBlindIndex(recruit.toDocument(), studentIdBlindIndex).map { it.toDomain() }

    override fun findByRecruitAndEmailBlindIndex(recruit: Recruit, emailBlindIndex: String): Optional<RecruitForm> =
        repository.findByRecruitAndEmailBlindIndex(recruit.toDocument(), emailBlindIndex).map { it.toDomain() }

    override fun findByRecruitAndPhoneNumberBlindIndex(
        recruit: Recruit,
        phoneNumberBlindIndex: String,
    ): Optional<RecruitForm> =
        repository.findByRecruitAndPhoneNumberBlindIndex(recruit.toDocument(), phoneNumberBlindIndex).map { it.toDomain() }

    override fun save(recruitForm: RecruitForm): RecruitForm =
        saveOrDuplicate { repository.save(recruitForm.toDocument()) }.toDomain()
    override fun deleteAll(recruitForms: Iterable<RecruitForm>) =
        repository.deleteAll(recruitForms.map { it.toDocument() })
}

@Component
class MongoRecruitSmsRepositoryAdapter(
    private val repository: SpringRecruitSmsDocumentRepository,
) : RecruitSmsRepository {
    override fun findByRecruit(recruit: Recruit): RecruitSms = repository.findByRecruit(recruit.toDocument()).toDomain()
    override fun save(recruitSms: RecruitSms): RecruitSms = saveOrDuplicate { repository.save(recruitSms.toDocument()) }.toDomain()
    override fun deleteAllByRecruit(recruit: Recruit): Long = repository.deleteAllByRecruit(recruit.toDocument())
}

private fun PageQuery.toPageable(): org.springframework.data.domain.Pageable =
    PageRequest.of(page, size, Sort.by(sorts.map { it.toOrder() }))

private fun PageQuery.Sort.toOrder(): Sort.Order =
    when (direction) {
        PageQuery.Direction.ASC -> Sort.Order.asc(property)
        PageQuery.Direction.DESC -> Sort.Order.desc(property)
    }

private fun <D : Any, T : Any> Page<D>.mapResult(mapper: (D) -> T): PageResult<T> =
    PageResult(
        content = content.map(mapper),
        page = number,
        size = size,
        totalElements = totalElements,
    )

private fun <T> saveOrDuplicate(block: () -> T): T =
    try {
        block()
    } catch (exception: DuplicateKeyException) {
        throw DuplicateEntityException(exception)
    }
