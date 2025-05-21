package com.D107.runmate.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.presentation.utils.GpxFileStorage
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

class WearableService: Service(), DataClient.OnDataChangedListener {
    private lateinit var dataClient: DataClient
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        dataClient = Wearable.getDataClient(this)
        dataClient.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        try {
            Timber.d("데이터 변경 감지")

            dataEventBuffer.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val uri = event.dataItem.uri
                    val path = uri.path

                    Timber.d("데이터 변경 감지: path=$path, host=${uri.host}")

                    if (path == "/gpx_file") {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                        val gpxAsset = dataMap.getAsset("gpx_asset")
                        val distance = dataMap.getDouble("distance")
                        val avgPace = dataMap.getDouble("avgPace")
                        val calories = dataMap.getString("calories", "")
                        val startTime = dataMap.getLong("startTime")
                        val endTime = dataMap.getLong("endTime")
                        val avgElevation = dataMap.getDouble("avgElevation")
                        val avgBpm = dataMap.getInt("avgBpm")
                        val courseId = dataMap.getString("courseId", "")
                        val startLocation = dataMap.getString("startLocation", "")
                        val groupId = dataMap.getString("groupId", "")
                        val avgCadence = dataMap.getInt("avgCadence")

                        // 로그 출력
                        Timber.d("====== 수신된 GPX 파일 데이터 ======")
                        Timber.d("거리: $distance km")
                        Timber.d("평균 페이스: $avgPace min/km")
                        Timber.d("칼로리: $calories")
                        Timber.d("시작 시간: ${Date(startTime)}")
                        Timber.d("종료 시간: ${Date(endTime)}")
                        Timber.d("평균 고도: $avgElevation m")
                        Timber.d("평균 심박수: $avgBpm bpm")
                        Timber.d("시작 위치: $startLocation")
                        Timber.d("코스 ID: $courseId")
                        Timber.d("그룹 ID: $groupId")
                        Timber.d("평균 케이던스: $avgCadence")
                        Timber.d("=================================")

                        // GPX 파일 저장 (백그라운드에서 실행)
                        if (gpxAsset != null) {
                            scope.launch {
                                val savedFilePath = GpxFileStorage.saveGpxFile(
                                    applicationContext,
                                    gpxAsset,
                                    distance,
                                    startTime,
                                    startLocation
                                )

                                if (savedFilePath != null) {
                                    Timber.d("GPX 파일 저장 완료: $savedFilePath")

                                    // 저장 완료 후 워치에 확인 메시지 전송
                                    sendConfirmationToWatch()
                                } else {
                                    Timber.e("GPX 파일 저장 실패")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("데이터 처리 중 오류: ${e.message}")
            e.printStackTrace()
        } finally {
            dataEventBuffer.release()
        }
    }

    private suspend fun sendConfirmationToWatch() {
        try {
            val nodeClient = Wearable.getNodeClient(this)
            val nodes = Tasks.await(nodeClient.connectedNodes)

            if (nodes.isNotEmpty()) {
                val messageClient = Wearable.getMessageClient(this)
                nodes.forEach { node ->
                    try {
                        Tasks.await(messageClient.sendMessage(
                            node.id,
                            "/gpx_file_received",
                            "success".toByteArray()
                        ))
                        Timber.d("GPX 파일 수신 확인 전송 성공: ${node.id}")
                    } catch (e: Exception) {
                        Timber.e("GPX 파일 수신 확인 전송 실패: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("GPX 수신 확인 전송 중 오류: ${e.message}")
        }
    }
}