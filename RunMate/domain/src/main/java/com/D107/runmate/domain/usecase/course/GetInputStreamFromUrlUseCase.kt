package com.D107.runmate.domain.usecase.course

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import javax.inject.Inject

class GetInputStreamFromUrlUseCase @Inject constructor() {
    suspend operator fun invoke(url: String): Flow<InputStream> = flow {
        println("getInputStreamFromUrl")
        emit(URL(url).openStream())
    }.flowOn(Dispatchers.IO)
}