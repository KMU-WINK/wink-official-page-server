package com.github.kmu_wink.wink_official_page.domain.user

import com.github.kmu_wink.wink_official_page.application.port.out.PageQuery
import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.application.port.out.UserDirectoryQueryPort
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.PublicSocialResponse
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.PublicUserResponse
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.UserDocument
import com.github.kmu_wink.wink_official_page.infrastructure.persistence.toDomain
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class UserDirectoryQueryAdapter(
    private val mongoTemplate: MongoTemplate,
) : UserDirectoryQueryPort {
    override fun findPublicMembers(): List<PublicUserResponse> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("role").ne("ADMIN")),
            Aggregation.addFields()
                .addField("roleOrder")
                .withValue(Document("\$indexOfArray", listOf(roleOrderList, "\$role")))
                .build(),
            Aggregation.sort(Sort.by(Sort.Order.desc("roleOrder"), Sort.Order.asc("name"))),
            Aggregation.project("name", "avatar", "description", "social", "role"),
        )

        return mongoTemplate.aggregate(aggregation, UserDocument::class.java, UserDocument::class.java)
            .mappedResults
            .map { it.toDomain().toPublicResponse() }
    }

    override fun findAdminUsers(query: String, pageQuery: PageQuery): PageResult<User> {
        val escapedQuery = Pattern.quote(query.trim())
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("role").ne("ADMIN")),
            Aggregation.match(searchCriteria(escapedQuery)),
            Aggregation.addFields()
                .addField("roleOrder")
                .withValue(Document("\$indexOfArray", listOf(roleOrderList, "\$role")))
                .build(),
            Aggregation.sort(Sort.by(Sort.Order.desc("roleOrder"), Sort.Order.asc("name"))),
            Aggregation.project().andExclude("roleOrder"),
            Aggregation.skip((pageQuery.page * pageQuery.size).toLong()),
            Aggregation.limit(pageQuery.size.toLong()),
        )

        val results = mongoTemplate.aggregate(aggregation, UserDocument::class.java, UserDocument::class.java)
        val total = mongoTemplate.count(
            Query.query(
                Criteria().andOperator(
                    Criteria.where("role").ne("ADMIN"),
                    searchCriteria(escapedQuery),
                ),
            ),
            UserDocument::class.java,
        )

        return PageResult(
            content = results.mappedResults.map { it.toDomain() },
            page = pageQuery.page,
            size = pageQuery.size,
            totalElements = total,
        )
    }

    private fun searchCriteria(escapedQuery: String): Criteria =
        Criteria().orOperator(
            Criteria.where("name").regex(escapedQuery, "i"),
            Criteria.where("studentId").regex(escapedQuery, "i"),
            Criteria.where("email").regex(escapedQuery, "i"),
            Criteria.where("phoneNumber").regex(escapedQuery, "i"),
        )

    private fun User.toPublicResponse(): PublicUserResponse =
        PublicUserResponse(
            id = id,
            name = name,
            avatar = avatar,
            description = description,
            social = social?.let { PublicSocialResponse(it.github, it.instagram, it.blog) },
            role = role?.name,
        )

    companion object {
        private val roleOrderList = User.Role.entries.map { it.name }
    }
}
