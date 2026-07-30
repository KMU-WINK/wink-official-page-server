package com.github.kmu_wink.wink_official_page.domain.user.__admin__.dto.request

import com.github.kmu_wink.wink_official_page.domain.recruit.util.validation.DepartmentValidate
import com.github.kmu_wink.wink_official_page.domain.user.schema.User
import com.github.kmu_wink.wink_official_page.global.util.RegExp
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @field:Size(min = 8, max = 8, message = RegExp.STUDENT_ID_MESSAGE)
    val studentId: String,
    @field:NotBlank
    @field:DepartmentValidate
    val department: String,
    @field:NotBlank
    @field:Pattern(regexp = RegExp.KOOKMIN_EMAIL_EXPRESSION, message = RegExp.KOOKMIN_EMAIL_MESSAGE)
    val email: String,
    @field:NotBlank
    @field:Pattern(regexp = RegExp.PHONE_NUMBER_EXPRESSION, message = RegExp.PHONE_NUMBER_MESSAGE)
    val phoneNumber: String,
    @field:NotNull
    val role: User.Role,
    @field:NotNull
    val fee: Boolean,
)
