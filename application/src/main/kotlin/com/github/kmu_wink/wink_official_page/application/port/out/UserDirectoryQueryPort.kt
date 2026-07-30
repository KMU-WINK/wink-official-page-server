package com.github.kmu_wink.wink_official_page.application.port.out

import com.github.kmu_wink.wink_official_page.domain.user.dto.response.PublicUserResponse
import com.github.kmu_wink.wink_official_page.domain.user.schema.User

interface UserDirectoryQueryPort {
    fun findPublicMembers(): List<PublicUserResponse>
    fun findAdminUsers(query: String, pageQuery: PageQuery): PageResult<User>
}
