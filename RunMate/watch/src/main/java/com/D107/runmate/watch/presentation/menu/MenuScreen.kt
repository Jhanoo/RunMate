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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    viewModel: MenuViewModel = hiltViewModel()
) {
    val listState = rememberScalingLazyListState()

    val bluetoothConnected by viewModel.isBluetoothConnected.collectAsState()

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
                            colorResource(
                                id = if (bluetoothConnected) R.color.gray_text2 else R.color.primary
                            ),
                            shape = CircleShape
                        )
                        .clickable(enabled = !bluetoothConnected) {
                            if (!bluetoothConnected) onNavigateToRunning()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "시작",
                        fontSize = 28.sp,
                        color = if (bluetoothConnected) Color.Gray else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                Button(
                    onClick = { if (!bluetoothConnected) onNavigateToPace() },
                    enabled = !bluetoothConnected,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (bluetoothConnected)
                            colorResource(id = R.color.gray_text2)
                        else
                            colorResource(id = R.color.black)
                    )
                ) {
                    Text(
                        text = "페이스",
                        color = if (bluetoothConnected) Color.Gray else Color.White,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (bluetoothConnected)
                        "휴대폰과 연결됨"
                    else
                        "휴대폰과 연결되지 않음",
                    color = if (bluetoothConnected)
                        colorResource(id = R.color.primary)
                    else
                        Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 블루투스 연결 버튼 (연결되지 않았을 때만 표시)
                if (!bluetoothConnected) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.connectToPhone() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(id = R.color.primary)
                        )
                    ) {
                        Text(
                            text = "휴대폰 연결",
                            color = Color.Black,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 안내 메시지 추가
            item {
                if (bluetoothConnected) {
                    Text(
                        text = "휴대폰 앱에서 러닝을 시작하세요",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "휴대폰 연결 없이 직접 러닝할 수 있습니다",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}