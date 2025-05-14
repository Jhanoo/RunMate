package com.D107.runmate.domain.repository

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Place
import kotlinx.coroutines.flow.Flow

interface KakaoApiRepository {
    suspend fun getSearchKeyword(query:String): Flow<ResponseStatus<List<Place>>>
}