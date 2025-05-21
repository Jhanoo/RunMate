package com.D107.runmate.presentation

import android.app.Service
import android.content.Context
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
import com.google.android.gms.wearable.Asset
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
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
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
        dataClient = Wearable.getDataClient(this)
        dataClient.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        try {
            dataEventBuffer.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val uri = event.dataItem.uri
                    val path = uri.path

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

                        scope.launch {
                            if (gpxAsset != null) {
                                saveGpxFile(this@WearableService, gpxAsset)
                                val met = if(avgPace > 20*60) 1.0
                                else if(avgPace > 10*60) 2.5
                                else if(avgPace > 7.5*60) 5.0
                                else if(avgPace > 5*60) 7.0
                                else 10.0
                                val weight = dataStoreRepository.weight.first() ?: 0.0
                                val date1 = Date(endTime)
                                val date2 = Date(startTime)
                                val diffMillis = kotlin.math.abs(date2.time - date1.time)
                                val diffMinutes = diffMillis / (60.0 * 1000.0)
                                val calories = diffMinutes * met * 3.5 * weight / 200
                                val convertedStartTime = formatLongToIso8601(startTime)
                                val convertedEndTime = formatLongToIso8601(endTime)
                                val startLocation = getAddress(startLon, startLat) ?: ""
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

    private suspend fun saveGpxFile(context: Context, gpxAsset: Asset): String? = withContext(Dispatchers.IO){
        try {
            // Asset에서 데이터 얻기
            val dataClient = Wearable.getDataClient(context)
            val assetFdResult = Tasks.await(dataClient.getFdForAsset(gpxAsset))
            val assetInputStream = assetFdResult.inputStream

            if (assetInputStream == null) {
                Timber.e("Failed to get asset input stream")
                return@withContext null
            }
            val fileName = "running_tracking.gpx"

            val gpxFile = File(context.filesDir, fileName)

            // 파일에 데이터 쓰기
            FileOutputStream(gpxFile).use { outputStream ->
                assetInputStream.copyTo(outputStream)
            }
            return@withContext gpxFile.absolutePath

        } catch (e: Exception) {
            Timber.e("Error saving GPX file: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}