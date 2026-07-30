package com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.response

import com.github.kmu_wink.wink_official_page.application.port.out.PageResult
import com.github.kmu_wink.wink_official_page.domain.user.dto.response.UserPayload

data class AdminUsersResponse(
    val users: PageResult<UserPayload>,
)
