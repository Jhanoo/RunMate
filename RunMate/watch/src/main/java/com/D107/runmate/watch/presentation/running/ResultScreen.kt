package com.D107.runmate.watch.presentation.running

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.D107.runmate.watch.R

@Composable
fun ResultScreen(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
//            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "러닝 기록",
//                modifier = Modifier.offset(y = 10.dp)
            fontSize = 14.sp,
            )

            HorizontalDivider(
                modifier = Modifier.width(110.dp),
                thickness = .8.dp,
//                color = Color.Gray,
                color = colorResource(id = R.color.gray_text2),

            )

            Text(
                text = "1.44km",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                textAlign = TextAlign.Center,
//                modifier = Modifier.offset(y = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                // 시간
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "시간",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                    )

                    Canvas(
                        modifier = Modifier
                            .height(15.dp)
                            .width(65.dp)
                    ) {
                        val dotRadius = .8.dp.toPx()
                        val spaceBetweenDots = 4.dp.toPx()
                        var currentX = dotRadius

                        while (currentX < size.width) {
                            drawCircle(
                                color = Color.DarkGray,
                                radius = dotRadius,
                                center = Offset(currentX, size.height / 2)
                            )
                            currentX += spaceBetweenDots
                        }
                    }

                    Text(
                        text = "00:21:33",
                        fontSize = 14.sp,
                    )
                }

                // 평균 페이스
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "평균 페이스",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                    )

                    Canvas(
                        modifier = Modifier
                            .height(15.dp)
                            .width(50.dp)
                    ) {
                        val dotRadius = .8.dp.toPx()
                        val spaceBetweenDots = 4.dp.toPx()
                        var currentX = dotRadius

                        while (currentX < size.width) {
                            drawCircle(
                                color = Color.DarkGray,
                                radius = dotRadius,
                                center = Offset(currentX, size.height / 2)
                            )
                            currentX += spaceBetweenDots
                        }
                    }

                    Text(
                        text = "05'10\"",
                        color = colorResource(id = R.color.primary),
                        fontSize = 14.sp,
                    )
                }

                // 평균 심박수
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "평균 심박수",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                    )

                    Canvas(
                        modifier = Modifier
                            .height(15.dp)
                            .width(63.dp)
                    ) {
                        val dotRadius = .8.dp.toPx()
                        val spaceBetweenDots = 4.dp.toPx()
                        var currentX = dotRadius

                        while (currentX < size.width) {
                            drawCircle(
                                color = Color.DarkGray,
                                radius = dotRadius,
                                center = Offset(currentX, size.height / 2)
                            )
                            currentX += spaceBetweenDots
                        }
                    }

                    Text(
                        text = "110",
                        fontSize = 14.sp,
                    )
                }

                // 칼로리
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "칼로리",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                    )

                    Canvas(
                        modifier = Modifier
                            .height(15.dp)
                            .width(85.dp)
                    ) {
                        val dotRadius = .8.dp.toPx()
                        val spaceBetweenDots = 4.dp.toPx()
                        var currentX = dotRadius

                        while (currentX < size.width) {
                            drawCircle(
                                color = Color.DarkGray,
                                radius = dotRadius,
                                center = Offset(currentX, size.height / 2)
                            )
                            currentX += spaceBetweenDots
                        }
                    }

                    Text(
                        text = "105",
                        fontSize = 14.sp,
                    )
                }
            }

        }

        // Pause button
        Box(
            modifier = Modifier
                .offset(y = 142.dp)
                .size(160.dp)
                .background(
                    color = colorResource(id = R.color.secondary),
                    shape = CircleShape
                )
                .clickable(onClick = {onClick()}),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.check),
                contentDescription = "Pause",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(20.dp)
            )
        }
    }
}