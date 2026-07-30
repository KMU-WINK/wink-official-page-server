package com.github.kmu_wink.wink_official_page.domain.recruit.schema

import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema
import java.time.LocalDate

class Recruit(
    var year: Int = 0,
    var semester: Int = 0,
    var recruitStartDate: LocalDate? = null,
    var recruitEndDate: LocalDate? = null,
    var interviewStartDate: LocalDate? = null,
    var interviewEndDate: LocalDate? = null,
    var step: Step? = null,
) : BaseSchema() {
    fun finalizePaper() {
        require(step == Step.PRE) { "Recruit paper can only be finalized from PRE" }
        step = Step.PAPER_END
    }

    fun finalizeInterview() {
        require(step == Step.PAPER_END) { "Recruit interview can only be finalized from PAPER_END" }
        step = Step.INTERVIEW_END
    }

    enum class Step {
        PRE,
        PAPER_END,
        INTERVIEW_END,
    }
}
