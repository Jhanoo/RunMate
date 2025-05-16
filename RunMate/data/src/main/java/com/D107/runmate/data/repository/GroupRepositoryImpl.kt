package com.D107.runmate.data.repository

import GroupResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.group.GroupDataSource
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.data.remote.response.group.GroupJoinResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.GroupData
import com.D107.runmate.domain.model.group.JoinInfo
import com.D107.runmate.domain.repository.group.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.annotations.ApiStatus
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDataSource: GroupDataSource
) : GroupRepository {
    override suspend fun createGroup(groupCreateInfo: GroupCreateInfo): Flow<ResponseStatus<GroupData?>> =
        flow {
            try {
                val response = groupDataSource.createGroup(
                    GroupCreateRequest(
                        groupName = groupCreateInfo.groupName,
                        courseId = groupCreateInfo.courseId,
                        startTime = groupCreateInfo.startTime.toString(),
                        startLocation = groupCreateInfo.startLocation,
                        latitude = groupCreateInfo.latitude,
                        longitude = groupCreateInfo.longitude
                    )
                )
                if (response is ApiResponse.Success && response.data != null) {
                    emit(ResponseStatus.Success(response.data.toDomainModel()))
                }
            } catch (e: Exception) {
                Timber.e("${e.message}")
                emit(
                    ResponseStatus.Error(
                        NetworkError(
                            message = e.message ?: "",
                        )
                    )
                )
            }
        }

    override suspend fun getCurrentGroup(): Flow<ResponseStatus<GroupData?>> = flow {
        try {
            val response = groupDataSource.getCurrentGroup()
            if (response is ApiResponse.Success && response.data != null) {
                emit(ResponseStatus.Success(response.data.toDomainModel()))
            } else {
                emit(ResponseStatus.Success(null))
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message ?: "")))
        }

    }

    override suspend fun leaveGroup(): Flow<ResponseStatus<Unit>> = flow {
        try {
            val response = groupDataSource.leaveGroup()
            if (response is ApiResponse.Success) {
                emit(ResponseStatus.Success(Unit))
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message ?: "")))
        }


    }

    override suspend fun joinGroup(intviteCode: String): Flow<ResponseStatus<JoinInfo?>> = flow {
        try {
            val response = groupDataSource.joinGroup(intviteCode)
            if (response is ApiResponse.Success && response.data != null) {
                emit(ResponseStatus.Success(response.data.toDomainModel()))
            } else {
                emit(ResponseStatus.Success(null))
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message ?: "")))
        }
    }

    override suspend fun startGroup(): Flow<ResponseStatus<Unit?>> = flow {
        try {
            val response = groupDataSource.startGroup()
            if (response is ApiResponse.Success) {
                emit(ResponseStatus.Success(Unit))
            } else if (response is ApiResponse.Error) {
                emit(
                    ResponseStatus.Error(
                        NetworkError(
                            message = response.error.message ?: "Unknown Error"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message ?: "")))
        }

    }

    override suspend fun finishGroup(): Flow<ResponseStatus<Unit?>> = flow {
        try {
            val response = groupDataSource.finishGroup()
            if (response is ApiResponse.Success) {
                emit(ResponseStatus.Success(null))
            }else if (response is ApiResponse.Error){
                emit(ResponseStatus.Error(NetworkError(message = response.error.message?:"Unknown Error")))
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message ?: "")))
        }

    }

    override suspend fun hasGroupHistory(): Flow<ResponseStatus<Boolean>> = flow{
        try{
            val response = groupDataSource.hasGroupHistoriy()
            if(response is ApiResponse.Success){
                emit(ResponseStatus.Success(response.data))
            }else if(response is ApiResponse.Error){
                emit(ResponseStatus.Error(NetworkError(message = response.error.message?:"Unknown Error")))
            }

        }catch (e:Exception){
            Timber.e("${e.message}")
            emit(ResponseStatus.Error(NetworkError(message = e.message?:"")))
        }
    }


}