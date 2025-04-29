package com.D107.runmate.data.remote.logger

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer

class RunMateApiLogger: HttpLoggingInterceptor.Logger {
    private val logName = "RunMate"
    private val moshi = Moshi.Builder().build()
    private val adapter: JsonAdapter<Any> = moshi.adapter(Any::class.java).indent("  ")
    override fun log(message: String) {
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val source = Buffer().writeUtf8(message)
                val reader = JsonReader.of(source)
                val value = reader.readJsonValue()

                val prettyPrintJson = adapter.toJson(value)
                Log.v(logName, prettyPrintJson)
            } catch (e: Exception) {
                Log.v(logName, message)
            }
        } else {
            Log.v(logName, message)
        }
    }
}