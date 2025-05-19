package com.D107.runmate.presentation.utils

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object WatchDataUtils {
    suspend fun sendTokenToWatch(context: Context, token: String) {
        withContext(Dispatchers.IO) {
            val dataClient = Wearable.getDataClient(context)
            val dataMapRequest = PutDataMapRequest.create("/jwt_token")
            dataMapRequest.dataMap.putString("jwt", token)
            val request = dataMapRequest.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).addOnSuccessListener {
                // 성공 로그 등
                Timber.d("sendTokenToWatch success")
            }
                .addOnFailureListener {
                    Timber.e("sendTokenToWatch Fail ${it.message}")
                }
        }
    }
}