package com.D107.runmate.presentation.utils

import android.content.Context
import android.util.Xml
import com.D107.runmate.domain.model.running.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL

object GpxParser {
    fun parseGpx(inputStream: InputStream): List<TrackPoint> {
        Timber.d("inputStream ${inputStream}")
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser().apply {
                setInput(it, null)
            }
            return parseTrackPoints(parser)
        }
    }
    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseTrackPoints(parser: XmlPullParser): List<TrackPoint> {
        val trackPoints = mutableListOf<TrackPoint>()
        var eventType = parser.eventType
        var currentTrackPoint: TrackPoint? = null

        // 네임스페이스 정의
        val gpxNamespace = "http://www.topografix.com/GPX/1/1"
        val gpxtpxNamespace = "http://www.garmin.com/xmlschemas/TrackPointExtension/v1"

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.namespace) {
                        gpxNamespace -> when (parser.name) {
                            "trkpt" -> {
                                currentTrackPoint = TrackPoint(
                                    lat = parser.getAttributeValue(null, "lat").toDouble(),
                                    lon = parser.getAttributeValue(null, "lon").toDouble(),
                                    ele = null,
                                    time = null,
                                    hr = null,
                                    cadence = null,
                                    pace = null
                                )
                            }

                            "ele" -> {
                                currentTrackPoint = currentTrackPoint?.copy(
                                    ele = parser.nextText().toDoubleOrNull()
                                )
                            }

                            "time" -> {
                                currentTrackPoint = currentTrackPoint?.copy(
                                    time = parser.nextText()
                                )
                            }

                            "extensions" -> {
                                var hr: Int? = null
                                var cad: Int? = null
                                var pace: Int? = null
                                while (!(eventType == XmlPullParser.END_TAG && parser.name == "extensions")) {
                                    parser.next()
                                    if (eventType == XmlPullParser.START_TAG
                                        && parser.namespace == gpxtpxNamespace
                                        && parser.name == "TrackPointExtension"
                                    ) {
                                        while (!(eventType == XmlPullParser.END_TAG
                                                    && parser.name == "TrackPointExtension")
                                        ) {
                                            eventType = parser.next()
                                            when {
                                                eventType == XmlPullParser.START_TAG
                                                        && parser.namespace == gpxtpxNamespace -> {
                                                    when (parser.name) {
                                                        "hr" -> hr = parser.nextText().toIntOrNull()
                                                        "cad" -> cad =
                                                            parser.nextText().toIntOrNull()

                                                        "pace" -> pace =
                                                            parser.nextText().toIntOrNull()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                currentTrackPoint = currentTrackPoint?.copy(
                                    hr = hr,
                                    cadence = cad,
                                    pace = pace
                                )
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.namespace == gpxNamespace && parser.name == "trkpt") {
                        currentTrackPoint?.let { trackPoints.add(it) }
                        currentTrackPoint = null
                    }
                }
            }
            eventType = parser.next()
        }
        Timber.d("trackPoints size ${trackPoints.size}")
        return trackPoints
    }

    suspend fun getGpxInputStream(url: String): InputStream =
        withContext(Dispatchers.IO) {
            val url = URL(url)
            url.openStream()
        }

    fun downloadFile(url: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(context.filesDir, "running_tracking_2.gpx")
            val url = URL(url)
            url.openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}