package com.D107.runmate.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.D107.runmate.data.mapper.MarathonMapper.toDomainModel
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.manager.MarathonDataSource
import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.domain.repository.manager.MarathonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class MarathonRepositoryImpl @Inject constructor(
    private val marathonDataSource: MarathonDataSource
) : MarathonRepository {
    override suspend fun getMarathons(): Flow<Result<List<MarathonInfo>>> = flow {
        when (val response = marathonDataSource.getMarathons()) {
            is ApiResponse.Success -> {
                val marathons = response.data.map { it.toDomainModel() }
                emit(Result.success(marathons))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception(response.error.message)))
            }
        }
    }

    override suspend fun getMarathonById(marathonId: String): Flow<Result<MarathonInfo>> = flow {
        when (val response = marathonDataSource.getMarathonById(marathonId)) {
            is ApiResponse.Success -> {
                emit(Result.success(response.data.toDomainModel()))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception(response.error.message)))
            }
        }
    }
}