package com.D107.runmate.watch.domain.repository

import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxMetadata
import com.D107.runmate.watch.domain.model.GpxTrackPoint
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Date

interface GpxRepository {
    // 트랙 포인트 수집
    suspend fun addTrackPoint(trackPoint: GpxTrackPoint)

    // 현재 세션에 기록된 모든 트랙 포인트 가져오기
    suspend fun getSessionTrackPoints(): List<GpxTrackPoint>

    // 세션 초기화
    suspend fun clearSession()

    // GPX 파일 생성 및 저장
    suspend fun createGpxFile(metadata: GpxMetadata): File

    // GPX 파일 업로드 상태 업데이트
    suspend fun updateGpxFileStatus(id: Long, status: GpxUploadStatus)

    // 업로드 대기 중인 GPX 파일 목록 가져오기
    fun getPendingGpxFiles(): Flow<List<GpxFile>>

    // GPX 파일 정보 저장
    suspend fun saveGpxFileInfo(gpxFile: GpxFile): Long

    // GPX 파일 서버 업로드
    suspend fun uploadGpxFile(file: File): Boolean
}