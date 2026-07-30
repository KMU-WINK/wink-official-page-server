package com.github.kmu_wink.wink_official_page.application.port.out

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    val totalPages: Int =
        if (size <= 0) 0 else ((totalElements + size - 1) / size).toInt()
    val numberOfElements: Int = content.size
    val first: Boolean = page <= 0
    val last: Boolean = page >= totalPages - 1
    val empty: Boolean = content.isEmpty()
}
