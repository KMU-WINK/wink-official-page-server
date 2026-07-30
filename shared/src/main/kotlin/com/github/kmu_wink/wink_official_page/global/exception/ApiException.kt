package com.github.kmu_wink.wink_official_page.global.exception

class ApiException : RuntimeException {
    val status: Int

    constructor(code: ApiExceptionCode) : super(code.getMessage()) {
        status = code.getStatus()
    }

    constructor(message: String) : super(message) {
        status = 400
    }
}
