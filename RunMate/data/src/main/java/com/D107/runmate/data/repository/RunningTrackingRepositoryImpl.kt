package com.D107.runmate.data.repository

import com.D107.runmate.data.mapper.LocationMapper
import com.D107.runmate.data.utils.GpxWriter
import com.D107.runmate.domain.model.running.CadenceRecordState
import com.D107.runmate.domain.model.running.GpxMetadata
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.PersonalRunningInfo
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackPoint
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "RunningTrackingRepository"

class RunningTrackingRepositoryImpl @Inject constructor(
    private val gpxWriter: GpxWriter
) : RunningTrackingRepository {
    private val _time = MutableStateFlow<Int>(0)
    override val time = _time.asStateFlow()

    private val _cadenceRecord = MutableStateFlow<CadenceRecordState>(CadenceRecordState.Initial)
    override val cadenceRecord = _cadenceRecord.asStateFlow()

    private val _runningRecord = MutableStateFlow<RunningRecordState>(RunningRecordState.Initial)
    override val runningRecord = _runningRecord.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocationState>(UserLocationState.Initial)
    override val userLocation = _userLocation.asStateFlow()

    private val _recordSize = MutableStateFlow<Int>(0)
    override val recordSize = _recordSize.asStateFlow()

    private val _trackingStatus = MutableStateFlow(TrackingStatus.INITIAL)
    override val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    override fun setTrackingStatus(status: TrackingStatus) {
        if(status == TrackingStatus.INITIAL) {
            _time.value = 0
            _cadenceRecord.value = CadenceRecordState.Initial
            _runningRecord.value = RunningRecordState.Initial
            _userLocation.value = UserLocationState.Initial
            _recordSize.value = 0
        } else if(status == TrackingStatus.INITIAL) {
            gpxWriter.deleteFile()
        }
        _trackingStatus.value = status
    }

    override fun setInitialUserLocation(location: LocationModel) {
        _userLocation.value = UserLocationState.Exist(listOf(location))
    }

    override fun finishTracking(): Flow<Boolean> = flow {
        when (val record = _runningRecord.value) {
            is RunningRecordState.Exist -> {
                val trackPoints =
                    record.runningRecords.zip((_userLocation.value as UserLocationState.Exist).locations)
                        .map { (runningRecord, location) ->
                            TrackPoint(
                                lat = location.latitude,
                                lon = location.longitude,
                                ele = location.altitude,
                                time = runningRecord.currentTime,
                                hr = 0, // 워치로부터 데이터받아와서 넣기 (hr리스트 만들어도 될 듯)
                                cadence = runningRecord.cadence,
                                pace = if (runningRecord.currentSpeed == 0f) 0 else (16.6667 / runningRecord.currentSpeed).toInt()
                            )
                        }
                if (gpxWriter.isFileExists() != null) {
                    gpxWriter.appendGpxFile(trackPoints)
                } else {
                    val metadata = GpxMetadata(
                        name = "러닝 ${
                            SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.getDefault()
                            ).format(Date())
                        }",
                        desc = "RunMate - ${Date()}",
                        time = trackPoints.first().time ?: SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.KOREA
                        ).format(Date())
                    )
                    gpxWriter.createGpxFile(trackPoints, metadata)
                }
                if(gpxWriter.finishWriteGpxFile()) {
                    emit(true)
                }else {
                    emit(false)
                }
            }
            else -> {emit(false)}
        }
    }

    private var isVibrationEnabled = true
    private var isSoundEnabled = true

    override fun incrementTime() {
        _time.value += 1
    }

    override fun processLocationUpdate(location: LocationModel, cadence: Int) {
        val timeStamp = System.currentTimeMillis()
        val date = Date(timeStamp)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedTime = formatter.format(date)

        when (_userLocation.value) {
            is UserLocationState.Initial -> {
                val personalRunningInfo = PersonalRunningInfo(
                    0f,
                    location.speed,
                    location.altitude,
                    location.speed,
                    location.altitude,
                    cadence,
                    cadence.toDouble(),
                    formattedTime
                )
                val tmpList = listOf(personalRunningInfo)
                _runningRecord.value = RunningRecordState.Exist(tmpList)
                _userLocation.value = UserLocationState.Exist(listOf(location))
            }

            is UserLocationState.Exist -> {
                when (val record = _runningRecord.value) {
                    is RunningRecordState.Exist -> {
                        val prevRecord = record.runningRecords.last()
                        val prevLocation =
                            (_userLocation.value as UserLocationState.Exist).locations.last()
                        val distanceDiff: Float = LocationMapper.toAndroid(location)
                            .distanceTo(LocationMapper.toAndroid(prevLocation))
                        val distance = (distanceDiff / 1000) + prevRecord.distance

                        val altitudeSum = record.runningRecords.last().altitude + location.altitude
                        val cadenceSum = record.runningRecords.last().cadenceSum + cadence
                        val currentTime = time.value.takeIf { it > 0 } ?: 1
                        if (record.runningRecords.size == 120) {
                            val trackPoints =
                                record.runningRecords.zip((_userLocation.value as UserLocationState.Exist).locations)
                                    .map { (runningRecord, location) ->
                                        TrackPoint(
                                            lat = location.latitude,
                                            lon = location.longitude,
                                            ele = location.altitude,
                                            time = runningRecord.currentTime,
                                            hr = 0, // 워치로부터 데이터받아와서 넣기 (hr리스트 만들어도 될 듯)
                                            cadence = runningRecord.cadence,
                                            pace = if (runningRecord.currentSpeed == 0f) 0 else (16.6667 / runningRecord.currentSpeed).toInt()
                                        )
                                    }
                            if (gpxWriter.isFileExists() != null) {
                                gpxWriter.appendGpxFile(trackPoints)
                            } else {
                                val metadata = GpxMetadata(
                                    name = "러닝 ${
                                        SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm",
                                            Locale.getDefault()
                                        ).format(Date())
                                    }",
                                    desc = "RunMate - ${Date()}",
                                    time = SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        Locale.KOREA
                                    ).format(trackPoints.first().time),
                                )
                                gpxWriter.createGpxFile(trackPoints, metadata)
                            }
                            _runningRecord.value = RunningRecordState.Exist(listOf())
                            _userLocation.value = UserLocationState.Exist(listOf())
                        }
                        val tmpList: List<PersonalRunningInfo> = record.runningRecords +
                                PersonalRunningInfo(
                                    distance,
                                    distance * 1000 / currentTime,
                                    location.altitude,
                                    location.speed,
                                    altitudeSum,
                                    cadence,
                                    cadenceSum,
                                    formattedTime
                                )
                        _runningRecord.value = RunningRecordState.Exist(tmpList)
                        _userLocation.value =
                            UserLocationState.Exist((_userLocation.value as UserLocationState.Exist).locations + location)
                    }

                    is RunningRecordState.Initial -> {
                        val personalRunningInfo = PersonalRunningInfo(
                            0f,
                            location.speed,
                            location.altitude,
                            location.speed,
                            location.altitude,
                            cadence,
                            cadence.toDouble(),
                            formattedTime
                        )
                        val tmpList = listOf(personalRunningInfo)
                        _runningRecord.value = RunningRecordState.Exist(tmpList)
                        _userLocation.value = UserLocationState.Exist(listOf(location))
                    }
                }
            }
            else -> {}
        }
    }
}