package com.D107.runmate.watch.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.D107.runmate.watch.R

// PNG 프레임들을 사용하는 방식
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var animationProgress by remember { mutableStateOf(0f) }
    var currentFrame by remember { mutableStateOf(0) }

    // 애니메이션 프레임 리스트 (예: tonie_frame_1.png, tonie_frame_2.png ...)
    val frameResources = remember {
        listOf(
            R.drawable.tonie_1,
            R.drawable.tonie_2,
            R.drawable.tonie_3,
            R.drawable.tonie_4,
            R.drawable.tonie_5,
            R.drawable.tonie_6,
//            R.drawable.tonie_7,
            R.drawable.tonie_8,
            R.drawable.tonie_9,
            R.drawable.tonie_10,
            R.drawable.tonie_11,
            R.drawable.tonie_12,
            R.drawable.tonie_13,
            R.drawable.tonie_14,
            R.drawable.tonie_15,
            R.drawable.tonie_16,
        )
    }

    LaunchedEffect(Unit) {
        // 프레임 애니메이션
        launch {
            while (true) {
                delay(15) // 프레임 레이트 조절
                currentFrame = (currentFrame + 1) % frameResources.size
            }
        }

        // 위치 애니메이션
        launch {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 5000,
                    easing = LinearEasing
                )
            ) { value, _ ->
                animationProgress = value
            }
        }

        delay(5000)
        onTimeout()
    }

    val gifSize = 150.dp
    val startX = -(screenWidth.value / 2) - (gifSize.value / 2)
    val endX = (screenWidth.value / 2) + (gifSize.value / 2)
    val gifOffsetX = startX + (endX - startX) * animationProgress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RUNMATE",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Image(
            painter = painterResource(id = frameResources[currentFrame]),
            contentDescription = null,
            modifier = Modifier
                .size(gifSize)
                .graphicsLayer {
                    translationX = gifOffsetX

                    // 하드웨어 가속
                    alpha = 0.99f
                }
        )
    }
}