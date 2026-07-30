package com.github.kmu_wink.wink_official_page.domain.recruit.__admin__.__sms__.schema

import com.github.kmu_wink.wink_official_page.domain.recruit.schema.Recruit
import com.github.kmu_wink.wink_official_page.domain.recruit.schema.RecruitForm
import com.github.kmu_wink.wink_official_page.domain.user.schema.PreUser
import com.github.kmu_wink.wink_official_page.domain.common.schema.BaseSchema

class RecruitSms(
    var recruit: Recruit? = null,
    var paperFail: String? = null,
    var paperPass: String? = null,
    var finalFail: String? = null,
    var finalPass: String? = null,
) : BaseSchema() {
    companion object {
        fun transform(content: String, form: RecruitForm): String =
            content.replace("{NAME}", form.name.orEmpty())
                .replace("{STUDENT_ID}", form.studentId.orEmpty())
                .replace("{DEPARTMENT}", form.department.orEmpty())
                .replace("{EMAIL}", form.email.orEmpty())
                .replace("{PHONE_NUMBER}", form.phoneNumber.orEmpty())

        fun transform(content: String, preUser: PreUser): String =
            content.replace("{NAME}", preUser.name.orEmpty())
                .replace("{STUDENT_ID}", preUser.studentId.orEmpty())
                .replace("{DEPARTMENT}", preUser.department.orEmpty())
                .replace("{EMAIL}", preUser.email.orEmpty())
                .replace("{PHONE_NUMBER}", preUser.phoneNumber.orEmpty())
                .replace("{TOKEN}", preUser.token.orEmpty())
    }
}
