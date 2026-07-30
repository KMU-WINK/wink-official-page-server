package com.github.kmu_wink.wink_official_page.application.port.out

import java.io.File

interface ObjectStoragePort {
    fun upload(path: String, file: File): String
    fun generatePresignedUrl(path: String): String
    fun deleteKey(key: String)
    fun deleteUrl(url: String, namespace: StorageNamespace)
    fun isOwnedUrl(url: String, namespace: StorageNamespace, ownerId: String): Boolean
}

enum class StorageNamespace(
    val prefix: String,
) {
    AVATAR("avatar/"),
    APPLICATION("application/"),
    PROGRAM("program/"),
}
