package com.D107.runmate.watch.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 수정된 imports
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.D107.runmate.watch.R

@Composable
fun MenuScreen(
    onNavigateToRunning: () -> Unit = {},
    onNavigateToPace: () -> Unit = {},
    onNavigateToAny: () -> Unit = {},
    buttonsEnabled: Boolean = true
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            autoCentering = AutoCenteringParams()
        ) {
            item {}

            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = if (buttonsEnabled) colorResource(id = R.color.primary)
                            else colorResource(id = R.color.gray_text),
                            shape = CircleShape
                        )
                        .clickable(enabled = buttonsEnabled) { onNavigateToRunning() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "시작",
                        fontSize = 28.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                Button(
                    onClick = onNavigateToPace,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.black)
                    ),
                    enabled = buttonsEnabled
                ) {
                    Text(
                        text = "페이스",
                        color = if (buttonsEnabled) Color.White else Color.Gray,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}