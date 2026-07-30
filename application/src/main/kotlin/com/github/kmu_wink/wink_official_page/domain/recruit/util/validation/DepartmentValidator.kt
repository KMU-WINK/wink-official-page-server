package com.github.kmu_wink.wink_official_page.domain.recruit.util.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class DepartmentValidator : ConstraintValidator<DepartmentValidate, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean = VALID_DEPARTMENTS.contains(value)

    companion object {
        private val VALID_DEPARTMENTS = listOf(
            "글로벌인문·지역대학 한국어문학부",
            "글로벌인문·지역대학 영어영문학부",
            "글로벌인문·지역대학 중국학부",
            "글로벌인문·지역대학 한국역사학과",
            "사회과학대학 행정학과",
            "사회과학대학 정치외교학과",
            "사회과학대학 사회학과",
            "사회과학대학 미디어ㆍ광고학부",
            "사회과학대학 교육학과",
            "사회과학대학 러시아ㆍ유라시아학과",
            "사회과학대학 일본학과",
            "법과대학 법학부",
            "법과대학 기업융합법학과",
            "경상대학 경제학과",
            "경상대학 국제통상학과",
            "경영대학 경영학부",
            "경영대학 경영정보학부",
            "경영대학 AI빅데이터융합경영학과",
            "경영대학 기업경영학부",
            "경영대학 회계세무학과",
            "독립학부 KMU International Business School",
            "창의공과대학 신소재공학부",
            "창의공과대학 기계공학부",
            "창의공과대학 건설시스템공학부",
            "창의공과대학 전자공학부",
            "소프트웨어융합대학 소프트웨어학부",
            "소프트웨어융합대학 인공지능학부",
            "자동차융합대학 자동차공학과",
            "자동차융합대학 자동차IT융합학과",
            "자동차융합대학 미래자동차학부",
            "자동차융합대학 미래모빌리티학과",
            "과학기술대학 산림환경시스템학과",
            "과학기술대학 임산생명공학과",
            "과학기술대학 나노전자물리학과",
            "과학기술대학 응용화학부",
            "과학기술대학 식품영양학과",
            "과학기술대학 정보보안암호수학과",
            "과학기술대학 바이오발효융합학과",
            "건축대학 건축학부",
            "조형대학 공업디자인학과",
            "조형대학 시각디자인학과",
            "조형대학 금속공예학과",
            "조형대학 도자공예학과",
            "조형대학 의상디자인학과",
            "조형대학 공간디자인학과",
            "조형대학 영상디자인학과",
            "조형대학 자동차·운송디자인학과",
            "조형대학 AI디자인학과",
            "예술대학 음악학부",
            "예술대학 미술학부",
            "예술대학 공연예술학부",
            "체육대학 스포츠교육학과",
            "체육대학 스포츠산업레저학과",
            "체육대학 스포츠건강재활학과",
            "미래융합대학 인문기술융합학부",
        )
    }
}
