    package com.D107.runmate.data.remote.response.kakaolocal

    import com.D107.runmate.data.mapper.DataMapper
    import com.D107.runmate.data.remote.common.BaseResponse
    import com.D107.runmate.domain.model.base.BaseModel
    import com.D107.runmate.domain.model.group.RoadAddress
    import com.squareup.moshi.Json
    import com.squareup.moshi.JsonClass

    @JsonClass(generateAdapter = true)
    data class RoadAddressResponse (
        @Json(name = "address_name")
        val address_name: String, //전체 주소
        @Json(name = "region_1depth_name")
        val region_1depth_name: String?, // 시도명 (nullable)
        @Json(name = "region_2depth_name")
        val region_2depth_name: String?, // 시군구명 (nullable)
        @Json(name = "region_3depth_name")
        val region_3depth_name: String?, // 읍면동명 (nullable)
        @Json(name = "road_name")
        val road_name: String?, // 도로명 (nullable)
        @Json(name = "underground_yn")
        val underground_yn: String?, // 지하 여부 Y/N (nullable)
        @Json(name = "main_building_no")
        val main_building_no: String?, // 건물 본번 (nullable)
        @Json(name = "sub_building_no")
        val sub_building_no: String?, // 건물 부번 (nullable)
        @Json(name = "building_name")
        val building_name: String?, // 건물 이름 (nullable)
        @Json(name = "zone_no")
        val zone_no: String?
    ):BaseResponse{
        companion object : DataMapper<RoadAddressResponse,RoadAddress> {
            override fun RoadAddressResponse.toDomainModel(): RoadAddress {
                return RoadAddress(
                    address_name, region_1depth_name, region_2depth_name, region_3depth_name, road_name, underground_yn, main_building_no, sub_building_no, building_name, zone_no
                )
            }

        }
    }