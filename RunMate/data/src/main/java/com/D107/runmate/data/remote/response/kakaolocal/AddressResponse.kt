package com.D107.runmate.data.remote.response.kakaolocal

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.group.Address
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressResponse(

    @Json(name = "address_name")
    val address_name: String, // 전체 지번 주소 (필수)
    @Json(name = "region_1depth_name")
    val region_1depth_name: String?,
    @Json(name = "region_2depth_name")
    val region_2depth_name: String?,
    @Json(name = "region_3depth_name")
    val region_3depth_name: String?,
    @Json(name = "mountain_yn")
    val mountain_yn: String?, // 산 여부 Y/N
    @Json(name = "main_address_no")
    val main_address_no: String?, // 지번 본번
    @Json(name = "sub_address_no")
    val sub_address_no: String?, // 지번 부번 (값이 없을 경우 빈 문자열 "" 또는 null일 수 있음, JSON 예시에서는 빈 문자열)
    @Json(name = "zip_code")
    val zip_code: String?    // 우편번호 (값이 없을 경우 빈 문자열 "" 또는 null일 수 있음)
) : BaseResponse {
    companion object : DataMapper<AddressResponse, Address> {
        override fun AddressResponse.toDomainModel(): Address {
            return Address(
                address_name,
                region_1depth_name,
                region_2depth_name,
                region_3depth_name,
                mountain_yn,
                main_address_no,
                sub_address_no,
                zip_code
            )
        }
    }


}