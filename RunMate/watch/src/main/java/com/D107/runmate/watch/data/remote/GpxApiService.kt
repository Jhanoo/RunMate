package com.D107.runmate.watch.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface GpxApiService {
    @Multipart
    @POST("api/gpx/upload")
    suspend fun uploadGpxFile(
        @Part file: File
    ): Response<UploadResponse>

    // 확장 함수로 File을 MultipartBody.Part로 변환
    fun File.toMultipartBodyPart(paramName: String = "gpx_file"): MultipartBody.Part {
        val requestBody = this.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, this.name, requestBody)
    }
}

// 업로드 응답 데이터 클래스
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val fileId: String? = null
)