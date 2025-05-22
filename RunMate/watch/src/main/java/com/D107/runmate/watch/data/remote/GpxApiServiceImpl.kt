package com.D107.runmate.watch.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxApiServiceImpl @Inject constructor(
    private val apiService: GpxApiService
) {
    suspend fun uploadGpxFile(file: File): Response<UploadResponse> {
        val requestBody = file.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("gpx_file", file.name, requestBody)
        return apiService.uploadGpxFile(filePart)
    }
}