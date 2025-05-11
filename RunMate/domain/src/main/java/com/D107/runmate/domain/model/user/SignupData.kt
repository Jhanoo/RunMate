package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel
import okhttp3.MultipartBody

data class SignupData(
    val email: String,
    val password: String,
    val nickname: String,
    val birthday: String,
    val gender: String,
    val profileImage: String? = null
) : BaseModel