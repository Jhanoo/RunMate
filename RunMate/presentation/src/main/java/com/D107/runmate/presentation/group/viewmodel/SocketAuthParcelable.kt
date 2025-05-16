package com.D107.runmate.presentation.group.viewmodel

import android.os.Parcelable
import com.D107.runmate.domain.model.socket.SocketAuth
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SocketAuthParcelable( // Parcelable 버전 모델
    val userId: String,
    val nickname: String,
    val profileImage: String?
) : Parcelable

fun SocketAuth.toParcelable(): SocketAuthParcelable {
    return SocketAuthParcelable(
        userId = this.userId,
        nickname = this.nickname,
        profileImage = this.profileImage
    )
}

fun SocketAuthParcelable.toDomain(): SocketAuth {
    return SocketAuth(
        userId = this.userId,
        nickname = this.nickname,
        profileImage = this.profileImage
    )
}