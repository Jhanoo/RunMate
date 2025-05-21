package com.D107.runmate.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import com.D107.runmate.domain.model.manager.CurriculumInfo
import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.domain.repository.manager.MarathonRepository
import com.D107.runmate.domain.usecase.manager.CreateCurriculumUseCase
import com.D107.runmate.domain.usecase.manager.GetMyCurriculumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CurriculumViewModel @Inject constructor(
    private val createCurriculumUseCase: CreateCurriculumUseCase,
    private val getMyCurriculumUseCase: GetMyCurriculumUseCase,
    private val marathonRepository: MarathonRepository
) : ViewModel() {
    private val _runExp = MutableStateFlow(false)
    val runExp = _runExp.asStateFlow()

    private val _freqExp = MutableStateFlow("1~2회")
    val freqExp = _freqExp.asStateFlow()

    private val _distExp = MutableStateFlow("~10km")
    val distExp = _distExp.asStateFlow()

    private val _curriculumCreationResult = MutableStateFlow<Result<String>?>(null)
    val curriculumCreationResult = _curriculumCreationResult.asStateFlow()

    private val _myCurriculum = MutableStateFlow<Result<CurriculumInfo>?>(null)
    val myCurriculum = _myCurriculum.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _marathonInfo = MutableStateFlow<Result<MarathonInfo>?>(null)
    val marathonInfo = _marathonInfo.asStateFlow()

    fun setRunExp(hasExp: Boolean) {
        _runExp.value = hasExp
    }

    fun setFreqExp(freq: String) {
        _freqExp.value = freq
    }

    fun setDistExp(dist: String) {
        _distExp.value = dist
    }

    fun resetMyCurriculum() {
        _myCurriculum.value = null
    }

    fun forceNavigateToCurriculumView(curriculumId: String) {
        _curriculumCreationResult.value = Result.success(curriculumId)
    }

    fun createCurriculum(marathonId: String, goalDist: String, goalDate: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val curriculumInfo = CurriculumInfo(
                curriculumId = "",
                marathonId = marathonId,
                goalDist = goalDist,
                goalDate = goalDate,
                runExp = _runExp.value,
                distExp = _distExp.value,
                freqExp = _freqExp.value
            )

            createCurriculumUseCase(curriculumInfo)
                .catch { e ->
                    _curriculumCreationResult.value = Result.failure(e)
                    _isLoading.value = false
                }
                .collect { result ->
                    _curriculumCreationResult.value = result
                    _isLoading.value = false

                    result.getOrNull()?.let { curriculumId ->
                        // ViewModel에서는 Context에 접근할 수 없으므로, Fragment에서 처리해야 함
                        // 이 부분은 Fragment에서 구현해야 함
                    }
                }
        }
    }

    fun updateCurriculum(curriculumInfo: CurriculumInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 기존 커리큘럼 정보에서 필요한 필드만 복사
                val createRequest = CurriculumInfo(
                    curriculumId = "",  // 새로 생성하므로 빈 문자열
                    marathonId = curriculumInfo.marathonId,
                    goalDist = curriculumInfo.goalDist,
                    goalDate = curriculumInfo.goalDate,
                    runExp = curriculumInfo.runExp,
                    distExp = curriculumInfo.distExp,
                    freqExp = curriculumInfo.freqExp
                )

                // 실제 API 호출
                createCurriculumUseCase(createRequest)
                    .collect { result ->
                        _curriculumCreationResult.value = result
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _curriculumCreationResult.value = Result.failure(e)
                Timber.e("커리큘럼 업데이트 실패: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun getMyCurriculum() {
        viewModelScope.launch {
            _isLoading.value = true

            getMyCurriculumUseCase()
                .catch { e ->
                    _myCurriculum.value = Result.failure(e)
                    _isLoading.value = false
                }
                .collect { result ->
                    _myCurriculum.value = result
                    _isLoading.value = false
                }
        }
    }

    fun clearCurriculumResult(){
        _curriculumCreationResult.value = null
    }

    // 마라톤 정보 가져오기
    fun getMarathonById(marathonId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                marathonRepository.getMarathonById(marathonId)
                    .catch { e ->
                        _marathonInfo.value = Result.failure(e)
                        _isLoading.value = false
                    }
                    .collect { result ->
                        _marathonInfo.value = result
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _marathonInfo.value = Result.failure(e)
                _isLoading.value = false
            }
        }
    }
}