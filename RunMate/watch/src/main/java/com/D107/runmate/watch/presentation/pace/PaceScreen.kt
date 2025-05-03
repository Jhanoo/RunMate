package com.D107.runmate.watch.presentation.pace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.D107.runmate.watch.R
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

@Composable
fun PaceScreen(
    onPaceSelected: (String) -> Unit = {}
) {
    var selectedMinute by remember { mutableStateOf(6) }
    var selectedSecond by remember { mutableStateOf(0) }

    val minuteListState = rememberScalingLazyListState(
        initialCenterItemIndex = 4 // 6분이 중앙에 오도록 (6-2=4)
    )
    val secondListState = rememberScalingLazyListState(
        initialCenterItemIndex = 0 // 00초가 중앙에 오도록
    )

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 분과 초 선택 스크롤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 분 스크롤
            PacePickerColumn(
                items = (2..10).toList(),
                state = minuteListState,
                onItemSelected = { minute ->
                    selectedMinute = minute
                },
                suffix = "",
                modifier = Modifier.weight(1f)
            )

            // 구분자
            Text(
                text = "\'",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            // 초 스크롤
            PacePickerColumn(
                items = (0..5).map { it * 10 },
                state = secondListState,
                onItemSelected = { second ->
                    selectedSecond = second
                },
                suffix = "",
                formatter = { String.format("%02d", it) },
                modifier = Modifier.weight(1f)
            )

            // 구분자
            Text(
                text = "\"",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // 확인 버튼
        Button(
            onClick = {
                onPaceSelected("${selectedMinute}:${String.format("%02d", selectedSecond)}")
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .size(50.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.primary)
            )
        ) {
            Text(
                text = "→",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PacePickerColumn(
    items: List<Int>,
    state: ScalingLazyListState,
    onItemSelected: (Int) -> Unit,
    suffix: String,
    formatter: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state) {
        snapshotFlow { state.centerItemIndex }
            .collect { index ->
                if (index in items.indices){
                    onItemSelected(items[index])
                }
        }
    }

    ScalingLazyColumn(
        state = state,
        modifier = modifier
            .fillMaxHeight()
            .width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 0)
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isCenterItem = state.centerItemIndex == index

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${formatter(item)}$suffix",
                    fontSize = if (isCenterItem) 28.sp else 18.sp,
                    color = if (isCenterItem) Color.White else colorResource(id = R.color.gray_text),
                    fontWeight = if (isCenterItem) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}