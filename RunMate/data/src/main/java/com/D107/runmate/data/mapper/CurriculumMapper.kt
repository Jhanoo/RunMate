package com.D107.runmate.data.mapper

import com.D107.runmate.data.remote.response.manager.CurriculumCreationResponse
import com.D107.runmate.data.remote.response.manager.CurriculumDetailResponse
import com.D107.runmate.domain.model.manager.CurriculumInfo

object CurriculumMapper {
    fun CurriculumCreationResponse.toDomainModel(): String {
        return curriculumId
    }

    fun CurriculumDetailResponse.toDomainModel(): CurriculumInfo {
        return CurriculumInfo(
            curriculumId = curriculumId ?: "",
            marathonId = marathonId ?: "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            goalDist = goalDist ?: "10km",
            goalDate = goalDate ?: "2025-06-10T09:00:00+09:00",
            runExp = runExp ?: true,
            distExp = distExp ?: "~10km",
            freqExp = freqExp ?: "1~2회"
        )
    }
}