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
    val pace: String = "5'10\"",
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
    savedState: Triple<Int, Int, Int>? = null,
    onPauseClick: (DisplayMode, String, Int, Int, Int, RunningData) -> Unit = { _, _, _, _, _, _ -> }
) {
    var topDisplayIndex by remember { mutableStateOf(savedState?.first ?: 0) }
    var leftDisplayIndex by remember { mutableStateOf(savedState?.second ?: 1) }
    var rightDisplayIndex by remember { mutableStateOf(savedState?.third ?: 2) }

    // 기본 데이터에 전달받은 pace를 반영
    val currentRunningData = remember(pace) {
        runningData.copy(pace = pace.replace(":", "'") + "\"")
    }

    // pace가 "0:00"이 아닌지 확인
    val isPaceFixed = pace != "0:00"

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
            // 위쪽 상태 버튼
            DisplayButton(
                modifier = Modifier.padding(top = 20.dp),
                displayMode = DisplayMode.values()[topDisplayIndex],
                runningData = runningData,
                isLarge = true,
                onClick = { topDisplayIndex = (topDisplayIndex + 1) % 4 }
            )

            // 아래쪽 상태 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPaceFixed) {
                    // pace가 전달된 경우: 왼쪽은 runningData의 pace 고정
                    DisplayButton(
                        displayMode = DisplayMode.PACE,
                        runningData = runningData,
                        onClick = {}, // 클릭 이벤트 비활성화
                        customLabel = "페이스"
                    )
                } else {
                    // pace가 전달되지 않은 경우: 기존 동작 유지
                    DisplayButton(
                        displayMode = DisplayMode.values()[leftDisplayIndex],
                        runningData = runningData,
                        onClick = { leftDisplayIndex = (leftDisplayIndex + 1) % 4 }
                    )
                }

                if (isPaceFixed) {
                    // pace가 전달된 경우: 오른쪽은 목표 페이스 고정
                    DisplayButton(
                        displayMode = DisplayMode.PACE,
                        runningData = currentRunningData,
                        onClick = {}, // 클릭 이벤트 비활성화
                        customLabel = "목표 페이스"
                    )
                } else {
                    // pace가 전달되지 않은 경우: 기존 동작 유지
                    DisplayButton(
                        displayMode = DisplayMode.values()[rightDisplayIndex],
                        runningData = runningData,
                        onClick = { rightDisplayIndex = (rightDisplayIndex + 1) % 4 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Pause button
        Box(
            modifier = Modifier
                .offset(y = 120.dp)
                .size(140.dp)
                .background(
                    color = colorResource(id = R.color.secondary),
                    shape = CircleShape
                )
                .clickable(onClick = {
                    val titleMode = DisplayMode.entries[topDisplayIndex]
                    val titleData = when (titleMode) {
                        DisplayMode.TIME -> runningData.time
                        DisplayMode.BPM -> runningData.bpm
                        DisplayMode.PACE -> runningData.pace
                        DisplayMode.DISTANCE -> runningData.distance
                    }
                    onPauseClick(titleMode, titleData, topDisplayIndex, leftDisplayIndex, rightDisplayIndex, runningData)
                }),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.pause),
                contentDescription = "Pause",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(24.dp)
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
    onClick: () -> Unit,
    customLabel: String? = null
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
            text = customLabel ?: displayMode.label,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-4).dp)
        )
    }
}