package com.D107.runmate.domain.repository.manager

import com.D107.runmate.domain.model.manager.CurriculumInfo
import kotlinx.coroutines.flow.Flow

interface CurriculumRepository {
    suspend fun createCurriculum(CurriculumInfo: CurriculumInfo): Flow<Result<String>>
    suspend fun getMyCurriculum(): Flow<Result<CurriculumInfo>>
    suspend fun updateCurriculum(curriculumInfo: CurriculumInfo): Flow<Result<String>>
}