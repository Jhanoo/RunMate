package com.D107.runmate.watch.presentation.running

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.wear.compose.material.Text
import com.D107.runmate.watch.R

data class RunningData(
    val time: String = "1:10:13",
    val bpm: String = "135",
    val pace: String = "05'17\"",
    val distance: String = "8.5"
)

enum class DisplayMode(val label: String) {
    TIME("시간"),
    BPM("BPM"),
    PACE("페이스"),
    DISTANCE("km")
}

@Composable
fun RunningScreen(
    pace: String = "0:00", // PaceScreen에서 넘겨받은 값
    runningData: RunningData = RunningData(),
    progress: Float = 0.1f, // 0.0 to 1.0
    isPaused: Boolean = false,
    onPauseClick: () -> Unit = {}
) {
    var topDisplayIndex by remember { mutableStateOf(0) }
    var leftDisplayIndex by remember { mutableStateOf(1) }
    var rightDisplayIndex by remember { mutableStateOf(2) }

    // 기본 데이터에 전달받은 pace를 반영
    val currentRunningData = remember(pace) {
        runningData.copy(pace = pace.replace(":", "'") + "\"")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .drawBehind {
                // 배경 원형 테두리
                drawArc(
                    color = Color.DarkGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx())
                )

                // 진행 상태 표시
                drawArc(
                    color = Color(0xFF00FF00), // Green color
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
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
            // Top display
            DisplayButton(
                modifier = Modifier.padding(top = 20.dp),
                displayMode = DisplayMode.values()[topDisplayIndex],
                runningData = runningData,
                isLarge = true,
                onClick = { topDisplayIndex = (topDisplayIndex + 1) % 4 }
            )

            // Middle row with left and right displays
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayButton(
                    displayMode = DisplayMode.values()[leftDisplayIndex],
                    runningData = runningData,
                    onClick = { leftDisplayIndex = (leftDisplayIndex + 1) % 4 }
                )

                DisplayButton(
                    displayMode = DisplayMode.values()[rightDisplayIndex],
                    runningData = currentRunningData,
                    onClick = { rightDisplayIndex = (rightDisplayIndex + 1) % 4 }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))


        }
        // Pause button
        Box(
            modifier = Modifier
                .offset(y=120.dp)
                .size(140.dp)
                .background(
                    color = colorResource(id = R.color.secondary),
                    shape = CircleShape
                )
                .clickable(onClick = onPauseClick),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.pause),
                contentDescription = "Pause",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(24.dp)  // 이미지 크기 조정
            )
        }
    }
}

@Composable
private fun DisplayButton(
    modifier: Modifier = Modifier,
    displayMode: DisplayMode,
    runningData: RunningData,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (displayMode) {
                DisplayMode.TIME -> runningData.time
                DisplayMode.BPM -> runningData.bpm
                DisplayMode.PACE -> runningData.pace
                DisplayMode.DISTANCE -> runningData.distance
            },
            fontSize = if (isLarge) 44.sp else 21.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isLarge) FontStyle.Italic else FontStyle.Normal,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = when (displayMode) {
                DisplayMode.TIME -> displayMode.label
                DisplayMode.BPM -> displayMode.label
                DisplayMode.PACE -> displayMode.label
                DisplayMode.DISTANCE -> displayMode.label
            },
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}