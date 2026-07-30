package com.github.kmu_wink.wink_official_page.global.infra.mongo

import com.github.kmu_wink.wink_official_page.application.port.out.IdGeneratorPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ObjectIdGeneratorAdapter : IdGeneratorPort {
    override fun generateId(): String = ObjectId.get().toHexString()
}
