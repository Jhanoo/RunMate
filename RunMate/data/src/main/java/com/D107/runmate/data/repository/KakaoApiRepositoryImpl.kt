package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSource
import com.D107.runmate.data.remote.response.kakaolocal.PlaceResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.repository.KakaoApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

import javax.inject.Inject

class KakaoApiRepositoryImpl @Inject constructor(
    private val kakaoLocalDataSource: KakaoLocalDataSource
):KakaoApiRepository {
    override suspend fun getSearchKeyword(query: String): Flow<ResponseStatus<List<Place>>> = flow{
        try{
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                kakaoLocalDataSource.getSearchKeyword(query)
            }
            val body = response.body()
            if(response.isSuccessful&&body!=null){
                emit(
                    ResponseStatus.Success(
                        body.documents.map{placeInfo->
                            placeInfo.toDomainModel()
                        }
                    )
                )
            }
        }catch(e:Exception) {

        }
    }
}