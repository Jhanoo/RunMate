package com.D107.runmate.domain.usecase.manager

import com.D107.runmate.domain.model.manager.CurriculumInfo
import com.D107.runmate.domain.repository.manager.CurriculumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyCurriculumUseCase @Inject constructor(
    private val curriculumRepository: CurriculumRepository
) {
    suspend operator fun invoke(): Flow<Result<CurriculumInfo>> {
        return curriculumRepository.getMyCurriculum()
    }
}