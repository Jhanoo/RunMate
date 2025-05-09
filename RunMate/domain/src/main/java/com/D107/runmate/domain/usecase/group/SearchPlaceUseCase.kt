package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.repository.KakaoApiRepository
import javax.inject.Inject

class SearchPlaceUseCase @Inject constructor(
    private val kakaoApiRepository: KakaoApiRepository
) {
    suspend operator fun invoke(query: String) = kakaoApiRepository.getSearchKeyword(query)

}