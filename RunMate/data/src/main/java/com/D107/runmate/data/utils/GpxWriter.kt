package com.D107.runmate.data.utils

import android.content.Context
import com.D107.runmate.domain.model.running.GpxMetadata
import com.D107.runmate.domain.model.running.TrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class GpxWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val GPX_FILENAME = "running_tracking.gpx"

    // 1. 파일 생성
    fun createGpxFile(
        trackPoints: List<TrackPoint>,
        metadata: GpxMetadata
    ): Boolean {
        val file = getFile()
        return GpxGenerator.createGpxFile(file, trackPoints, metadata)
    }

    // 2. 파일에 이어서 작성
    fun appendGpxFile(
        trackPoints: List<TrackPoint>
    ): Boolean {
        val file = getFile()
        return GpxGenerator.appendGpxFile(file, trackPoints)
    }

    fun finishWriteGpxFile(): Boolean {
        val file = getFile()
        return GpxGenerator.finishWriteGpxFile(file)
    }

    fun getFile() : File {
        val directory = context.filesDir
        return File(directory, GPX_FILENAME)
    }

    fun deleteFile(): Boolean {
        val file = getFile()
        return if (file.exists()) {
            Timber.d("file exists")
            file.delete()
        } else {
            Timber.d("file not exists")
            true
        }
    }

    fun isFileExists(): File? {
        val file = getFile()
        if (file.exists() && file.isFile) return file
        return null
    }
}