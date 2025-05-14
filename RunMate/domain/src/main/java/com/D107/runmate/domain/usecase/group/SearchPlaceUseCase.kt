package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.repository.KakaoApiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPlaceUseCase @Inject constructor(
    private val kakaoApiRepository: KakaoApiRepository
) {
    suspend operator fun invoke(query: String): Flow<ResponseStatus<List<Place>>> = kakaoApiRepository.getSearchKeyword(query)

}