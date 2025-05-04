package com.D107.runmate.data.repository

import android.annotation.SuppressLint
import com.D107.runmate.data.mapper.SmartInsoleMapper
import com.D107.runmate.data.remote.datasource.BleConstants
import com.D107.runmate.data.remote.datasource.BleDataSource
import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.SmartInsole
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.InsoleData
import com.D107.runmate.domain.model.Insole.InsoleSide
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.repository.SmartInsoleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

//권한 체크는 presentation layer에서
@SuppressLint("MissingPermission")
class SmartInsoleRepositoryImpl @Inject constructor(
    private val bleDataSource: BleDataSource,
    private val smartInsoleMapper: SmartInsoleMapper,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : SmartInsoleRepository {


    override fun scanInsole(): Flow<List<SmartInsole>> {
        Timber.d("Repository: scanInsole() called. Returning Flow from DataSource.")
        return bleDataSource.scanDevices()
            .map { scanResults ->
                scanResults.mapNotNull { smartInsoleMapper.map(it) }
            }
            .distinctUntilChanged()
            .catch { e ->
                Timber.e(e, "Repository catch: Error during scanInsole flow processing")
                emit(emptyList())
            }
    }



    override fun connect(leftAddress: String, rightAddress: String) {
        bleDataSource.connectPair(leftAddress, rightAddress)
    }

    override fun disconnect() {
        bleDataSource.disconnectPair()
    }

    override fun observeConnectionState(): StateFlow<InsoleConnectionState> {
        return bleDataSource.pairConnectionState
    }

    override fun observeCombinedInsoleData(): Flow<ResponseStatus<CombinedInsoleData>> {
        Timber.d("Repository: observeCombinedInsoleData() called. Returning transformed Flow from DataSource.")
        return bleDataSource.combinedSensorDataFlow
            .map{ (leftBytes, rightBytes) ->
                val leftDataResult = leftBytes?.let { parseInsoleData(it, InsoleSide.LEFT) }
                val rightDataResult = rightBytes?.let { parseInsoleData(it, InsoleSide.RIGHT) }
                val leftData = leftDataResult?.getOrNull()
                val rightData = rightDataResult?.getOrNull()
                if (leftBytes != null && leftData == null) {
                    val errorMsg = leftDataResult?.exceptionOrNull()?.message ?: "Left insole data parsing failed"
                    Timber.w("$errorMsg Bytes: ${leftBytes.contentToString()}")
                    return@map ResponseStatus.Error(NetworkError(code = "PARSING_ERROR_LEFT", message = errorMsg))
                }
                if (rightBytes != null && rightData == null) {
                    val errorMsg = rightDataResult?.exceptionOrNull()?.message ?: "Right insole data parsing failed"
                    Timber.w("$errorMsg Bytes: ${rightBytes.contentToString()}")
                    return@map ResponseStatus.Error(NetworkError(code = "PARSING_ERROR_RIGHT", message = errorMsg))
                }
                ResponseStatus.Success(CombinedInsoleData(left = leftData, right = rightData))
            }
            .flowOn(defaultDispatcher)
            .catch { e ->
                Timber.e(e, "Repository catch: Error during observeCombinedInsoleData flow processing")
                val networkError = NetworkError(
                    code = "STREAM_ERROR",
                    message = e.message ?: "An unknown error occurred in the data stream."
                )
                emit(ResponseStatus.Error(networkError))
            }
    }



    private fun parseInsoleData(data: ByteArray, side: InsoleSide): Result<InsoleData> {
        if (data.size != BleConstants.RESPONSE_DATA_SIZE) {
            Timber.w("[$side] 수신 데이터 크기 불일치: ${data.size} bytes (Expected: ${BleConstants.RESPONSE_DATA_SIZE})")
            return Result.failure(IllegalArgumentException("[$side] Invalid data size: ${data.size}"))
        }

        return try {
            val buffer =
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN) // ESP32는 Little Endian 가정

            val fsrValues = IntArray(5) { buffer.int } // FSR 5개 (Int)
            val yprValues = FloatArray(3) { buffer.float } // YPR 3개 (Float)

            val insoleData = InsoleData(
                bigToe = fsrValues[0],
                smallToe = fsrValues[1],
                heel = fsrValues[2],
                archLeft = fsrValues[3],
                archRight = fsrValues[4],
                yaw = yprValues[0],
                pitch = yprValues[1],
                roll = yprValues[2]
            )
            Result.success(insoleData)
        } catch (e: Exception) {
            Timber.e(e, "[$side] 인솔 데이터 파싱 중 오류 발생")
            Result.failure(e)
        }
    }
}