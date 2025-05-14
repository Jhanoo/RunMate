package com.D107.runmate.domain.repository

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Address
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.model.group.RoadAddress
import kotlinx.coroutines.flow.Flow

interface KakaoApiRepository {
    suspend fun getSearchKeyword(query:String): Flow<ResponseStatus<List<Place>>>
    suspend fun getCoord2Address(x:Double,y:Double): Flow<ResponseStatus<Address>>
}