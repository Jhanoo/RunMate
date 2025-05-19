package com.D107.runmate.presentation.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.LoginData
import com.D107.runmate.domain.usecase.user.IsLoggedInUseCase
import com.D107.runmate.domain.usecase.user.LoginUseCase
import com.D107.runmate.domain.usecase.user.ValidateEmailUseCase
import com.D107.runmate.domain.usecase.user.ValidatePasswordUseCase
import com.D107.runmate.presentation.utils.WatchDataUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    init {
        viewModelScope.launch {
            isLoggedInUseCase().collectLatest { isLoggedIn ->
                _isLoggedIn.value = isLoggedIn
            }
        }
    }

    fun login(email: String, password: String) {
        // 로그인 시도
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            loginUseCase(email, password).collectLatest { result ->
                _loginState.value = when (result) {
                    is ResponseStatus.Success -> {
                        LoginUiState.Success(result.data)
                    }
                    is ResponseStatus.Error -> LoginUiState.Error(result.error.message)
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Initial
        _emailError.value = null
        _passwordError.value = null
    }

    sealed class LoginUiState {
        object Initial : LoginUiState()
        object Loading : LoginUiState()
        data class Success(val data: LoginData) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }
}