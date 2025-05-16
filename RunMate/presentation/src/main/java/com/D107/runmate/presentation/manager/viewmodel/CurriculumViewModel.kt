package com.D107.runmate.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.manager.CurriculumInfo
import com.D107.runmate.domain.usecase.manager.CreateCurriculumUseCase
import com.D107.runmate.domain.usecase.manager.GetMyCurriculumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurriculumViewModel @Inject constructor(
    private val createCurriculumUseCase: CreateCurriculumUseCase,
    private val getMyCurriculumUseCase: GetMyCurriculumUseCase
) : ViewModel() {
    private val _runExp = MutableStateFlow(true)
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
}