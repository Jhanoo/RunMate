package com.D107.runmate.presentation.running

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Address
import com.D107.runmate.domain.repository.running.RunningRepository
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.running.DeleteFileUseCase
import com.D107.runmate.domain.usecase.running.EndRunningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RunningEndViewModel @Inject constructor(
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    fun deleteFile() {
        viewModelScope.launch {
            deleteFileUseCase().collectLatest {
                if(it){
                    Timber.d("파일삭제")
                }else{
                    Timber.d("파일삭제실패")
                }
            }
        }
    }
}

sealed class RunningEndState {
    object Success : RunningEndState()
    data class Error(val message: String) : RunningEndState()
}

sealed class Coord2AddressState {
    data class Success(val address: Address) : Coord2AddressState()
    data class Error(val message: String) : Coord2AddressState()
}