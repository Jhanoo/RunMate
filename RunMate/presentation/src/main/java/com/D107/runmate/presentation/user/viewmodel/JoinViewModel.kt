package com.D107.runmate.presentation.user.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.ProfileImageSource
import com.D107.runmate.domain.model.user.SignupData
import com.D107.runmate.domain.model.user.UserInfo
import com.D107.runmate.domain.repository.user.AuthRepository
import com.D107.runmate.domain.usecase.user.SaveUserInfoUseCase
import com.D107.runmate.domain.usecase.user.SignupUseCase
import com.D107.runmate.domain.usecase.user.ValidateEmailUseCase
import com.D107.runmate.domain.usecase.user.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val signupUseCase: SignupUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val saveUserInfoUseCase: SaveUserInfoUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signupState = MutableStateFlow<SignupUiState>(SignupUiState.Initial)
    val signupState: StateFlow<SignupUiState> = _signupState.asStateFlow()

    private val _step = MutableLiveData<Int>(1)
    val step: LiveData<Int> = _step

    // 회원가입 1단계 데이터
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _passwordConfirmError = MutableLiveData<String?>()
    val passwordConfirmError: LiveData<String?> = _passwordConfirmError

    // 회원가입 2단계 데이터
    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> = _nickname

    private val _birthday = MutableLiveData<String>()
    val birthday: LiveData<String> = _birthday

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> = _gender

    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    private val _weight = MutableLiveData<Double>()
    val weight: LiveData<Double> = _weight

    private val _height = MutableLiveData<Double>()
    val height: LiveData<Double> = _height

    fun setEmail(email: String) {
        _email.value = email
        validateEmail(email)
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setNickname(nickname: String) {
        _nickname.value = nickname
    }

    fun setBirthday(year: Int, month: Int, day: Int) {
        _birthday.value = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    fun setGender(gender: String) {
        _gender.value = when (gender) {
            "남성" -> "MALE"
            "여성" -> "FEMALE"
            else -> "MALE"
        }
    }

    fun setWeight(weight: Double) {
        _weight.value = weight
    }

    fun setHeight(height: Double) {
        _height.value = height
    }

    fun setProfileImage(uri: Uri?) {
        _profileImageUri.value = uri
    }

    fun validateEmail(email: String): Boolean {
        val isValid = validateEmailUseCase(email)
        if (!isValid) {
            _emailError.value = "유효한 이메일 형식이 아닙니다."
        } else {
            _emailError.value = null
        }
        return isValid
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): Boolean {
        if (password != confirmPassword) {
            _passwordConfirmError.value = "비밀번호가 일치하지 않습니다."
            return false
        }

        _passwordConfirmError.value = null

        // 비밀번호 규칙 검증
        val isValid = validatePasswordUseCase(password)
        if (!isValid) {
            _passwordError.value = "비밀번호는 8자 이상이어야 합니다."
            return false
        }

        _passwordError.value = null
        return true
    }

    fun signup() {
        val email = _email.value ?: return
        val password = _password.value ?: return
        val nickname = _nickname.value ?: return
        val birthday = _birthday.value ?: return
        val gender = _gender.value ?: return
        val profileUri = _profileImageUri.value
        val weight = _weight.value ?: return
        val height = _height.value ?: return

        // Uri를 ProfileImageSource로 변환
        val profileImageSource = profileUri?.let {
            ProfileImageSource(it.toString())
        }

        val signupData = SignupData(
            email = email,
            password = password,
            nickname = nickname,
            birthday = birthday,
            gender = gender,
            profileImageSource = profileImageSource,
            weight = weight,
            height = height
        )

        viewModelScope.launch {
            _signupState.value = SignupUiState.Loading

            signupUseCase(signupData).collectLatest { result ->
                when (result) {
                    is ResponseStatus.Success -> {
                        saveUserInfoUseCase(result.data)
                        _signupState.value = SignupUiState.Success
                    }
                    is ResponseStatus.Error -> {
                        _signupState.value = SignupUiState.Error(result.error.message)
                    }
                }
            }
        }
    }

    suspend fun checkEmailAvailability(email: String): Flow<ResponseStatus<Boolean>> {
        return authRepository.checkEmail(email)
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }

    fun resetState() {
        // 단계 초기화
        _step.value = 1

        // 상태 초기화
        _signupState.value = SignupUiState.Initial

        // 1단계 데이터 초기화
        _email.value = ""
        _password.value = ""
        _emailError.value = null
        _passwordError.value = null
        _passwordConfirmError.value = null

        // 2단계 데이터 초기화
        _nickname.value = ""
        _birthday.value = ""
        _gender.value = ""
        _profileImageUri.value = null
        _weight.value = 0.0
        _weight.value = 0.0
    }

    fun goToNextStep() {
        _step.value = 2
    }

    fun goToPreviousStep() {
        _step.value = 1
    }

    sealed class SignupUiState {
        object Initial : SignupUiState()
        object Loading : SignupUiState()
        object Success : SignupUiState()
        data class Error(val message: String) : SignupUiState()
    }
}