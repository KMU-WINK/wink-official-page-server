package com.github.kmu_wink.wink_official_page.application.port.out

data class PageQuery(
    val page: Int,
    val size: Int,
    val sorts: List<Sort> = emptyList(),
) {
    data class Sort(
        val property: String,
        val direction: Direction = Direction.ASC,
    )

    enum class Direction {
        ASC,
        DESC,
    }
}
