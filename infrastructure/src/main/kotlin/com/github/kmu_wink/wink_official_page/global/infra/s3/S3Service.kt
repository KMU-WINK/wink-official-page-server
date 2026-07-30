package com.github.kmu_wink.wink_official_page.global.infra.s3

import com.github.kmu_wink.wink_official_page.application.port.out.ObjectStoragePort
import com.github.kmu_wink.wink_official_page.application.port.out.StorageNamespace
import com.github.kmu_wink.wink_official_page.global.property.AwsProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.io.File
import java.net.URI
import java.time.Duration

@Component
class S3Service(
    private val awsProperty: AwsProperty,
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
) : ObjectStoragePort {
    fun files(prefix: String): List<S3Object> = s3Client.listObjectsV2(
        ListObjectsV2Request.builder()
            .bucket(awsProperty.s3.bucket)
            .prefix(prefix)
            .build(),
    ).contents()

    override fun upload(path: String, file: File): String {
        s3Client.putObject(
            putObjectRequest(path),
            RequestBody.fromFile(file),
        )

        return s3Client.utilities()
            .getUrl { builder -> builder.bucket(awsProperty.s3.bucket).key(path) }
            .toString()
    }

    override fun generatePresignedUrl(path: String): String {
        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(1))
            .putObjectRequest(putObjectRequest(path))
            .build()

        return s3Presigner.presignPutObject(presignRequest).url().toString()
    }

    private fun putObjectRequest(path: String): PutObjectRequest {
        val builder = PutObjectRequest.builder()
            .bucket(awsProperty.s3.bucket)
            .key(path)

        if (awsProperty.s3.publicReadAclEnabled) {
            builder.acl(ObjectCannedACL.PUBLIC_READ)
        }

        return builder.build()
    }

    override fun deleteKey(key: String) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(awsProperty.s3.bucket)
                .key(key)
                .build(),
        )
    }

    override fun deleteUrl(url: String, namespace: StorageNamespace) {
        keyFromUrl(url, namespace)?.let(::deleteKey)
    }

    override fun isOwnedUrl(url: String, namespace: StorageNamespace, ownerId: String): Boolean =
        keyFromUrl(url, namespace)?.startsWith("${namespace.prefix}$ownerId/") == true

    fun keyFromUrl(url: String, namespace: StorageNamespace): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.userInfo != null || uri.port != -1) {
            return null
        }
        val host = uri.host ?: return null
        val path = uri.path.trimStart('/')
        val bucket = awsProperty.s3.bucket
        val key = when {
            host == "$bucket.s3.amazonaws.com" ||
                (host.startsWith("$bucket.s3.") && host.endsWith(".amazonaws.com")) -> path
            host == "s3.amazonaws.com" ||
                (host.startsWith("s3.") && host.endsWith(".amazonaws.com")) -> {
                if (!path.startsWith("$bucket/")) {
                    return null
                }
                path.removePrefix("$bucket/")
            }
            else -> return null
        }

        return key.takeIf { isManagedDeletableKey(it, namespace) }
    }

    private fun isManagedDeletableKey(key: String, namespace: StorageNamespace): Boolean =
        key.isNotBlank() &&
            !key.startsWith("/") &&
            !key.contains("..") &&
            key.startsWith(namespace.prefix) &&
            key.length > namespace.prefix.length

    fun delete(path: String) = deleteKey(path)

    fun urlToKey(url: String, namespace: StorageNamespace): java.util.Optional<String> =
        java.util.Optional.ofNullable(keyFromUrl(url, namespace))
}
