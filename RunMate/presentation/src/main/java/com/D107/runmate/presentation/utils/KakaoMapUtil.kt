package com.D107.runmate.presentation.utils

import android.content.Context
import com.D107.runmate.domain.model.running.TrackPoint
import com.D107.runmate.presentation.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet

object KakaoMapUtil {
    fun addCourseLine(context: Context, kakaoMap: KakaoMap, trackPoints: List<TrackPoint>) {

        val layer = kakaoMap.routeLineManager!!.layer

        val stylesSet = RouteLineStylesSet.from(
            "runMateStyles",
            RouteLineStyles.from(RouteLineStyle.from(12f, context.getColor(R.color.primary)))
        )

        val latLngList = trackPoints.map {
            LatLng.from(it.lat, it.lon)
        }

        val segment = RouteLineSegment.from(latLngList)
            .setStyles(stylesSet.getStyles(0))

        val options = RouteLineOptions.from(segment)
            .setStylesSet(stylesSet)

        val routeLine = layer.addRouteLine(options)
    }

    fun addCoursePoint(context: Context, kakaoMap: KakaoMap, prevPoint: LatLng, currentPoint: LatLng) {
        val layer = kakaoMap.routeLineManager!!.layer

        val stylesSet = RouteLineStylesSet.from(
            "runMateStyles_point",
            RouteLineStyles.from(RouteLineStyle.from(12f, context.getColor(R.color.secondary)))
        )
        val segment = RouteLineSegment.from(listOf(prevPoint, currentPoint))
            .setStyles(stylesSet.getStyles(0))

        val options = RouteLineOptions.from(segment)
            .setStylesSet(stylesSet)
//        lastLatLng = point
        val routeLine = layer.addRouteLine(options)
    }


}