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
            marathonId = marathonId ?: "",
            goalDist = goalDist ?: "",
            goalDate = goalDate ?: "",
            runExp = runExp ?: false,
            distExp = distExp ?: "",
            freqExp = freqExp ?: ""
        )
    }
}