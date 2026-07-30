package com.github.kmu_wink.wink_official_page.global.response

@ConsistentCopyVisibility
data class ApiResponse<T> private constructor(
    val success: Boolean,
    val error: String?,
    val content: T?,
) {
    companion object {
        fun ok(): ApiResponse<Void> = ApiResponse(success = true, error = null, content = null)

        fun <T> ok(content: T): ApiResponse<T> = ApiResponse(success = true, error = null, content = content)

        fun <T> error(error: String): ApiResponse<T> = ApiResponse(success = false, error = error, content = null)
    }
}
