package com.D107.runmate.domain.usecase.running

import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(
    private val runningTrackingRepository: RunningTrackingRepository
) {
    operator fun invoke() = runningTrackingRepository.deleteFile()
}