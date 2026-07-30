package com.github.kmu_wink.wink_official_page.global.exception

import com.github.kmu_wink.wink_official_page.application.port.out.ErrorReporterPort
import com.github.kmu_wink.wink_official_page.application.port.out.ErrorRequestContext
import com.github.kmu_wink.wink_official_page.global.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ApiExceptionHandler(
    private val errorReporter: ErrorReporterPort,
) {
    @ExceptionHandler(NoResourceFoundException::class, HttpRequestMethodNotSupportedException::class)
    fun noResourceFoundException(ignored: Exception): ResponseEntity<ApiResponse<Nothing>> =
        error(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다.")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(ignored: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> =
        error(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다.")

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun authorizationDeniedException(ignored: AuthorizationDeniedException): ResponseEntity<ApiResponse<Nothing>> =
        error(HttpStatus.FORBIDDEN, "권한이 없습니다.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fieldError = exception.bindingResult.fieldError
        val errorMessage = if (fieldError == null) {
            exception.message
        } else {
            "${fieldError.field}은(는) ${fieldError.defaultMessage}"
        }

        return error(HttpStatus.BAD_REQUEST, errorMessage)
    }

    @ExceptionHandler(ApiException::class)
    fun apiException(exception: ApiException): ResponseEntity<ApiResponse<Nothing>> {
        val status = HttpStatus.resolve(exception.status) ?: HttpStatus.BAD_REQUEST
        return error(status, exception.message ?: "요청을 처리할 수 없습니다.")
    }

    @ExceptionHandler(Exception::class)
    fun exception(exception: Exception, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> {
        reportUnexpected(exception, request)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.")
    }

    private fun error(status: HttpStatus, message: String): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(status).body(ApiResponse.error(message))

    private fun reportUnexpected(throwable: Throwable, request: HttpServletRequest) {
        errorReporter.capture(
            throwable,
            ErrorRequestContext(
                method = request.method,
                url = redactSensitivePath(request.requestURL.toString()),
                headers = request.headerNames.asSequence()
                    .filterNot(::isSensitiveHeader)
                    .associateWith(request::getHeader),
            ),
        )
    }

    private fun isSensitiveHeader(name: String): Boolean =
        name.equals("authorization", ignoreCase = true) ||
            name.equals("cookie", ignoreCase = true) ||
            name.equals("set-cookie", ignoreCase = true) ||
            name.equals("x-xsrf-token", ignoreCase = true) ||
            name.equals("x-recruit-edit-token", ignoreCase = true) ||
            name.equals("x-wink-client-context", ignoreCase = true)

    private fun redactSensitivePath(url: String): String =
        SENSITIVE_PATH_SEGMENT.replace(url) { result ->
            "${result.groupValues[1]}[redacted]"
        }

    companion object {
        private val SENSITIVE_PATH_SEGMENT =
            Regex("(?i)(/(?:edit|register|reset-password)/)[^/?]{20,}")
    }
}
