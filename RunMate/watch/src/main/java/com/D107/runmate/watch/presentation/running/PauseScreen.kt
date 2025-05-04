package com.D107.runmate.watch.presentation.running

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun PauseScreen(
    progress: Float = 0.1f, // 0.0 to 1.0
    onStartClick: () -> Unit = {},
    onStopClick: () -> Unit = {}

) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .drawBehind {
                // 배경 원형 테두리
                drawArc(
                    color = Color(0xFF202124),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx())
                )

                // 진행 상태 표시
                drawArc(
                    color = Color(0xFF53D357), // primary color
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            .padding(20.dp), // 상태바 안쪽 여백
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1:10:13",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = 20.dp)
            )
            Text(
                text = "시간",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-6).dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-12).dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
//                        .offset(y = 120.dp)
                        .size(50.dp)
                        .background(
                            color = colorResource(id = R.color.primary),
                            shape = CircleShape
                        )
                    .clickable(onClick = onStartClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = "Pause",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
//                        .offset(y = 120.dp)
                        .size(50.dp)
                        .background(
                            color = colorResource(id = R.color.btn_red),
                            shape = CircleShape
                        )
                    .clickable(onClick = onStopClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.stop),
                        contentDescription = "Pause",
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }

        }
    }
}