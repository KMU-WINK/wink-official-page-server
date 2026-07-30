package com.github.kmu_wink.wink_official_page.application.port.out

interface ErrorReporterPort {
    fun capture(throwable: Throwable, request: ErrorRequestContext)
}

data class ErrorRequestContext(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
)
