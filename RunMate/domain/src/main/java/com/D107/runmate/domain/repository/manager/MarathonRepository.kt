package com.D107.runmate.domain.repository.manager

import com.D107.runmate.domain.model.manager.MarathonInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface MarathonRepository {
    suspend fun getMarathons(): Flow<Result<List<MarathonInfo>>>
}