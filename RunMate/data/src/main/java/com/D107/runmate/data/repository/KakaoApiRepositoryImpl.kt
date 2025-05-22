package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSource
import com.D107.runmate.data.remote.response.kakaolocal.AddressResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.kakaolocal.PlaceResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.kakaolocal.RoadAddressResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Address
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.model.group.RoadAddress
import com.D107.runmate.domain.repository.KakaoApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
            emit(
                ResponseStatus.Error(
                    NetworkError(
                        message = e.message ?: ""
                    )
                )
            )

        }
    }

    override fun getCoord2Address(x: Double, y: Double): Flow<ResponseStatus<Address>> = flow {
        val response = withContext(Dispatchers.IO) {
            kakaoLocalDataSource.getCoord2Address(x, y)
        }
        val body = response.body()
        if (response.isSuccessful && body != null) {
            emit(
                ResponseStatus.Success(
                    body.documents.first().address.toDomainModel()
                )
            )
        } else {
            emit(
                ResponseStatus.Error(
                    NetworkError(
                        message = "Response is not successful or body is null"
                    )
                )
            )
        }
    }.catch { e ->
        emit(
            ResponseStatus.Error(
                NetworkError(
                    message = e.message ?: ""
                )
            )
        )
    }


//    override suspend fun getCoord2Address(x: Double, y: Double): Flow<ResponseStatus<Address>> = flow{
//        try {
//            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
//                kakaoLocalDataSource.getCoord2Address(x, y)
//            }
//            val body = response.body()
//            if (response.isSuccessful && body != null) {
//                emit(
//                    ResponseStatus.Success(
//                        body.documents.first().address.toDomainModel()
//                    )
//                )
//            }
//        }catch(e:Exception) {
//            emit(
//                ResponseStatus.Error(
//                    NetworkError(
//                        message = e.message ?: ""
//                    )
//                )
//            )
//        }
//    }
}