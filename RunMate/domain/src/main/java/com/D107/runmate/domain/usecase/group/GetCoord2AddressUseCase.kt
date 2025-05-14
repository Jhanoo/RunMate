package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.repository.KakaoApiRepository
import javax.inject.Inject

class GetCoord2AddressUseCase@Inject constructor(
    private val kakaoApiRepository: KakaoApiRepository
)  {
    suspend operator fun invoke(x: Double, y: Double) = kakaoApiRepository.getCoord2Address(x, y)
}