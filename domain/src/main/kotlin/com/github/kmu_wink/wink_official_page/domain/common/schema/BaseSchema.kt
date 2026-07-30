package com.github.kmu_wink.wink_official_page.domain.common.schema

import java.time.LocalDateTime

abstract class BaseSchema {
    var id: String? = null
        protected set

    var createdAt: LocalDateTime? = null
        protected set

    var updatedAt: LocalDateTime? = null
        protected set

    fun restoreIdentity(id: String?, createdAt: LocalDateTime?, updatedAt: LocalDateTime?) {
        this.id = id
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as BaseSchema
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
