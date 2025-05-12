package com.D107.runmate.data.utils

import com.D107.runmate.domain.model.running.GpxMetadata
import com.D107.runmate.domain.model.running.TrackPoint
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class GpxGenerator {
    companion object {
        fun createGpxFile(
            file: File,
            trackPoints: List<TrackPoint>,
            metadata: GpxMetadata
        ): Boolean {
            if (trackPoints.isEmpty()) {
                return false
            }

            return try {
                FileWriter(file).use { writer ->
                    // GPX 헤더
                    Timber.d("writer before header")
                    writer.append(
                        """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <gpx version="1.1" 
                            creator="RunMate Watch App" 
                            xmlns="http://www.topografix.com/GPX/1/1" 
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                            xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"
                            xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd
                            http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd">
                    """.trimIndent()
                    )
                    Timber.d("writer after header")
                    // 메타데이터
                    writer.append(
                        """
                        
                        <metadata>
                            <name>${metadata.name}</name>
                            <desc>${metadata.desc}</desc>
                            <time>${metadata.time}</time>
                        </metadata>
                    """.trimIndent()
                    )
                    Timber.d("writer after metadata")

                    // 트랙 시작
                    writer.append(
                        """
                        
                        <trk>
                            <name>${metadata.name}</name>
                            <trkseg>
                    """.trimIndent()
                    )

                    // 트랙 포인트
                    for (point in trackPoints) {
                        writer.append(
                            """
                            
                                <trkpt lat="${point.lat}" lon="${point.lon}">
                                    <ele>${point.ele}</ele>
                                    <time>${point.time}</time>
                                    <extensions>
                                        <gpxtpx:TrackPointExtension>
                                            <gpxtpx:hr>${point.hr}</gpxtpx:hr>
                                            <gpxtpx:cad>${point.cadence}</gpxtpx:cad>
                                            <gpxtpx:pace>${point.pace}</gpxtpx:pace>
                                        </gpxtpx:TrackPointExtension>
                                    </extensions>
                                </trkpt>
                        """.trimIndent()
                        )
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        fun appendGpxFile(
            file: File,
            trackPoints: List<TrackPoint>,
        ): Boolean {
            if (trackPoints.isEmpty()) {
                return false
            }

            return try {
                FileWriter(file, true).use { writer ->
                    // 트랙 포인트
                    for (point in trackPoints) {
                        writer.append(
                            """
                            
                                <trkpt lat="${point.lat}" lon="${point.lon}">
                                    <ele>${point.ele}</ele>
                                    <time>${point.time}</time>
                                    <extensions>
                                        <gpxtpx:TrackPointExtension>
                                            <gpxtpx:hr>${point.hr}</gpxtpx:hr>
                                            <gpxtpx:cad>${point.cadence}</gpxtpx:cad>
                                            <gpxtpx:pace>${point.pace}</gpxtpx:pace>
                                        </gpxtpx:TrackPointExtension>
                                    </extensions>
                                </trkpt>
                        """.trimIndent()
                        )
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        fun finishWriteGpxFile(
            file: File
        ): Boolean {
            return try {
                FileWriter(file, true).use { writer ->
                    writer.append(
                        """
                        
                            </trkseg>
                        </trk>
                    </gpx>
                    """.trimIndent()
                    )
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}