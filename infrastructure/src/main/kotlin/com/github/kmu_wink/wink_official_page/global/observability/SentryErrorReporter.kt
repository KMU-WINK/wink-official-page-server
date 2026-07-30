package com.github.kmu_wink.wink_official_page.global.observability

import com.github.kmu_wink.wink_official_page.application.port.out.ErrorReporterPort
import com.github.kmu_wink.wink_official_page.application.port.out.ErrorRequestContext
import io.sentry.Sentry
import org.springframework.stereotype.Component

@Component
class SentryErrorReporter : ErrorReporterPort {
    override fun capture(throwable: Throwable, request: ErrorRequestContext) {
        Sentry.captureException(throwable) { scope ->
            scope.request?.let { sentryRequest ->
                sentryRequest.method = request.method
                sentryRequest.url = request.url
                sentryRequest.queryString = null
                sentryRequest.headers = request.headers
                sentryRequest.data = null
                scope.request = sentryRequest
            }
        }
    }
}
