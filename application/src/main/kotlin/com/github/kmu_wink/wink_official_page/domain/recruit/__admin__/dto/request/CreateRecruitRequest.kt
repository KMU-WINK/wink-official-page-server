package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateRecruitRequest(
    @field:Min(2000)
    @field:Max(2999)
    var year: Int = 0,
    @field:Min(1)
    @field:Max(2)
    var semester: Int = 0,
    @field:NotNull
    var recruitStartDate: LocalDate? = null,
    @field:NotNull
    var recruitEndDate: LocalDate? = null,
    @field:NotNull
    var interviewStartDate: LocalDate? = null,
    @field:NotNull
    var interviewEndDate: LocalDate? = null,
)
