package com.github.kmu_wink.wink_official_page.domain.recruit.dto.request

import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StudentIdCheckRequest(
    @field:NotBlank
    @field:Size(min = 8, max = 8, message = RegExp.STUDENT_ID_MESSAGE)
    var studentId: String = "",
)
