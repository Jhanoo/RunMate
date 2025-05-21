package com.D107.runmate.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class WearableService: Service(), DataClient.OnDataChangedListener {
    private lateinit var dataClient: DataClient

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
            Timber.d("datachanged")
            if (!dataEventBuffer.isClosed) {
                Timber.d("datachanged: dataEventBuffer is Not closed")
                if(dataEventBuffer.count > 0){
                    Timber.d("datachanged: ${dataEventBuffer.count}")

                    dataEventBuffer.forEach { event ->
                        if (event.type == DataEvent.TYPE_CHANGED) {
                            val item = event.dataItem
                            Timber.d("datachanged: $item")
                            if (item.uri.path?.compareTo("/gpx_file") == 0) {
                                val dataMap = DataMapItem.fromDataItem(item).dataMap
                                val gpxFile = dataMap.getAsset("gpx_asset")
                                val distance = dataMap.getDouble("distance")
                                val avgPace = dataMap.getDouble("avgPace")
                                val startTime = dataMap.getLong("startTime")
                                val endTime = dataMap.getLong("endTime")
                                val avgElevation = dataMap.getDouble("avgElevation")
                                val avgBpm = dataMap.getInt("avgBpm")
                                val startLocation = dataMap.getString("startLocation")
                                val avgCadence = dataMap.getInt("avgCadence")

                                Timber.d("datachanged: $gpxFile, $distance, $avgPace, $startTime, $endTime, $avgElevation, $avgBpm, $startLocation, $avgCadence")
                            }
                        }
                    }
                }
            }
            else{
                Log.d("dataeventbuffer","isClosed")
            }
        } catch (e: Exception) {
            Log.e("MyForegroundService", "Error: ${e.message}")
        } finally {
            dataEventBuffer.release()
        }
    }
}