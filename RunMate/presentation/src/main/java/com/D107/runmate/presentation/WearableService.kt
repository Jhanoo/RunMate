package com.D107.runmate.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.running.EndRunningUseCase
import com.D107.runmate.presentation.running.RunningEndState
import com.D107.runmate.presentation.utils.CommonUtils.formatLongToIso8601
import com.D107.runmate.presentation.utils.GpxFileStorage
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
@AndroidEntryPoint
class WearableService : Service(), DataClient.OnDataChangedListener {
    private lateinit var dataClient: DataClient
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var endRunningUseCase: EndRunningUseCase

    @Inject
    lateinit var getCoord2AddressUseCase: GetCoord2AddressUseCase

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    private val _endRunning = MutableSharedFlow<RunningEndState>()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
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

                        val startTime = dataMap.getLong("startTime")
                        val endTime = dataMap.getLong("endTime")
                        val avgElevation = dataMap.getDouble("avgElevation")
                        val avgBpm = dataMap.getInt("avgBpm")

                        val startLat = dataMap.getDouble("startLat")
                        val startLon = dataMap.getDouble("startLon")
                        val avgCadence = dataMap.getInt("avgCadence")

                        // 로그 출력
                        Timber.d("====== 수신된 GPX 파일 데이터 ======")
                        Timber.d("거리: $distance km")
                        Timber.d("평균 페이스: $avgPace min/km")
                        Timber.d("시작 시간: ${Date(startTime)}")
                        Timber.d("종료 시간: ${Date(endTime)}")
                        Timber.d("평균 고도: $avgElevation m")
                        Timber.d("평균 심박수: $avgBpm bpm")
                        Timber.d("평균 케이던스: $avgCadence")
                        Timber.d("=================================")

                        // GPX 파일 저장 (백그라운드에서 실행)
                        scope.launch {
                            if (gpxAsset != null) {
                                val met = if(avgPace > 20*60) 1.0
                                else if(avgPace > 10*60) 2.5
                                else if(avgPace > 7.5*60) 5.0
                                else if(avgPace > 5*60) 7.0
                                else 10.0
                                val weight = dataStoreRepository.weight.first() ?: 0.0
                                val calories = (endTime-startTime/60) * met * 3.5 * weight / 200
                                val convertedStartTime = formatLongToIso8601(startTime)
                                val convertedEndTime = formatLongToIso8601(endTime)
                                val startLocation = getAddress(startLat, startLon) ?: ""
                                endRunning(
                                    avgBpm.toDouble(),
                                    avgCadence.toDouble(),
                                    avgElevation,
                                    avgPace,
                                    calories,
                                    null,
                                    distance,
                                    convertedEndTime,
                                    startLocation,
                                    convertedStartTime,
                                    null)
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
                        Tasks.await(
                            messageClient.sendMessage(
                                node.id,
                                "/gpx_file_received",
                                "success".toByteArray()
                            )
                        )
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
    private suspend fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String?,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String,
        groupId: String? = null
    ) {
        Timber.d("History GroupId!!!${groupId}")
        endRunningUseCase(
            avgBpm,
            avgCadence,
            avgElevation,
            avgPace,
            calories,
            courseId,
            distance,
            endTime,
            startLocation,
            startTime,
            groupId
        )
            .onStart {
            }
            .catch { e ->
                Timber.e("runningend error catch ${e.message}")
            }
            .collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        _endRunning.emit(RunningEndState.Success(status.data.historyId))
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("runningend error ${status.error.message}")
                        _endRunning.emit(RunningEndState.Error(status.error.message))
                    }
                }
            }
    }

    private suspend fun getAddress(lon: Double, lat: Double): String? {
        return getCoord2AddressUseCase(lon, lat)
            .first { it is ResponseStatus.Success }
            .let { status ->
                when (status) {
                    is ResponseStatus.Success -> status.data.address_name
                    else -> null
                }
            }
    }
}