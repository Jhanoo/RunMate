package com.D107.runmate.domain.model.group

import com.D107.runmate.domain.model.base.BaseModel

data class Address (

    val address_name: String, // 전체 지번 주소 (필수)

    val region_1depth_name: String?,

    val region_2depth_name: String?,

    val region_3depth_name: String?,

    val mountain_yn: String?, // 산 여부 Y/N

    val main_address_no: String?, // 지번 본번

    val sub_address_no: String?, // 지번 부번 (값이 없을 경우 빈 문자열 "" 또는 null일 수 있음, JSON 예시에서는 빈 문자열)

    val zip_code: String?    // 우편번호 (값이 없을 경우 빈 문자열 "" 또는 null일 수 있음)
):BaseModel