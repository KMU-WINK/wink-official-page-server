package com.github.kmu_wink.wink_official_page.application.port.out.repository

import com.github.kmu_wink.wink_official_page.domain.application.schema.OauthLogin
import java.util.Optional

interface OauthLoginRepository {
    fun findByToken(token: String): Optional<OauthLogin>
    fun save(oauthLogin: OauthLogin): OauthLogin
    fun delete(oauthLogin: OauthLogin)
}
