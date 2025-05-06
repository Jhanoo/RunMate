package com.D107.runmate.watch.util

import com.D107.runmate.watch.domain.model.GpxMetadata
import com.D107.runmate.watch.domain.model.GpxTrackPoint
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class GpxGenerator {
    companion object {
        // ISO 8601 포맷 (GPX 표준)
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun createGpxFile(
            file: File,
            trackPoints: List<GpxTrackPoint>,
            metadata: GpxMetadata
        ): Boolean {
            if (trackPoints.isEmpty()) {
                return false
            }

            return try {
                FileWriter(file).use { writer ->
                    // GPX 헤더
                    writer.append("""
                        <?xml version="1.0" encoding="UTF-8"?>
                        <gpx version="1.1" 
                            creator="RunMate Watch App" 
                            xmlns="http://www.topografix.com/GPX/1/1" 
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                            xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"
                            xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd
                            http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd">
                    """.trimIndent())

                    // 메타데이터
                    writer.append("""
                        
                        <metadata>
                            <name>${metadata.name}</name>
                            <desc>${metadata.desc}</desc>
                            <time>${DATE_FORMAT.format(metadata.startTime)}</time>
                        </metadata>
                    """.trimIndent())

                    // 트랙 시작
                    writer.append("""
                        
                        <trk>
                            <name>${metadata.name}</name>
                            <trkseg>
                    """.trimIndent())

                    // 트랙 포인트
                    for (point in trackPoints) {
                        writer.append("""
                            
                                <trkpt lat="${point.latitude}" lon="${point.longitude}">
                                    <ele>${point.elevation}</ele>
                                    <time>${DATE_FORMAT.format(point.time)}</time>
                                    <extensions>
                                        <gpxtpx:TrackPointExtension>
                                            <gpxtpx:hr>${point.heartRate}</gpxtpx:hr>
                                            <gpxtpx:cad>${point.cadence}</gpxtpx:cad>
                                            <gpxtpx:pace>${point.pace}</gpxtpx:pace>
                                        </gpxtpx:TrackPointExtension>
                                    </extensions>
                                </trkpt>
                        """.trimIndent())
                    }

                    // 트랙 종료
                    writer.append("""
                        
                            </trkseg>
                        </trk>
                    </gpx>
                    """.trimIndent())
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}