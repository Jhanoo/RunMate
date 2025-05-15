package com.D107.runmate.data.repository

import com.D107.runmate.data.mapper.CurriculumMapper.toDomainModel
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.manager.CurriculumDataSource
import com.D107.runmate.data.remote.request.manager.CurriculumRequest
import com.D107.runmate.domain.model.manager.CurriculumInfo
import com.D107.runmate.domain.repository.manager.CurriculumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val curriculumDataSource: CurriculumDataSource
) : CurriculumRepository {
    override suspend fun createCurriculum(CurriculumInfo: CurriculumInfo): Flow<Result<String>> =
        flow {
            val request = CurriculumRequest(
                marathonId = CurriculumInfo.marathonId,
                goalDist = CurriculumInfo.goalDist,
                goalDate = CurriculumInfo.goalDate,
                runExp = CurriculumInfo.runExp,
                distExp = CurriculumInfo.distExp,
                freqExp = CurriculumInfo.freqExp
            )

            when (val response = curriculumDataSource.createCurriculum(request)) {
                is ApiResponse.Success -> {
                    emit(Result.success(response.data.curriculumId))
                }

                is ApiResponse.Error -> {
                    emit(Result.failure(Exception(response.error.message)))
                }
            }
        }

    override suspend fun getMyCurriculum(): Flow<Result<CurriculumInfo>> = flow {
        when (val response = curriculumDataSource.getMyCurriculum()) {
            is ApiResponse.Success -> {
                emit(Result.success(response.data.toDomainModel()))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception(response.error.message)))
            }
        }
    }
}