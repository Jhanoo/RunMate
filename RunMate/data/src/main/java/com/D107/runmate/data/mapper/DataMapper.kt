package com.D107.runmate.data.mapper

import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.base.BaseModel

/**
 * BaseResponse(data) -> BaseModel(domain)로 변경하는 인터페이스
 * @param BaseResponse: Api로 받아오는 Entity
 * @param BaseModel: Domain 계층에서 사용하는 Dto
 *
 * @return BaseModel
 */
interface DataMapper<in R: BaseResponse, out D: BaseModel> {
    fun R.toDomainModel(): D
}