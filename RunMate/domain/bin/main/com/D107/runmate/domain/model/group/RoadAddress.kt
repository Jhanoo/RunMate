package com.D107.runmate.domain.model.group

import com.D107.runmate.domain.model.base.BaseModel


data class RoadAddress (
    val address_name: String, //전체 주소

    val region_1depth_name: String?, // 시도명 (nullable)

    val region_2depth_name: String?, // 시군구명 (nullable)

    val region_3depth_name: String?, // 읍면동명 (nullable)

    val road_name: String?, // 도로명 (nullable)

    val underground_yn: String?, // 지하 여부 Y/N (nullable)

    val main_building_no: String?, // 건물 본번 (nullable)

    val sub_building_no: String?, // 건물 부번 (nullable)

    val building_name: String?, // 건물 이름 (nullable)

    val zone_no: String?
):BaseModel