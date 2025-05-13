package com.D107.runmate.domain.usecase.user

import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String, confirmPassword: String? = null): Boolean {
        // 비밀번호 규칙: 8자 이상, 특수문자 포함, 대문자 포함
        val passwordRegex = "^(?=.*[A-Z])(?=.*[!@#\$%^&*])(?=.*[0-9]).{8,}$".toRegex()
        val isValidFormat = password.matches(passwordRegex)

        // 비밀번호 확인이 필요한 경우 (회원가입 시)
        return if (confirmPassword != null) {
            isValidFormat && password == confirmPassword
        } else {
            isValidFormat
        }
    }
}