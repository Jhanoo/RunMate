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
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.D107.runmate.watch.R

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var animationProgress by remember { mutableStateOf(0f) }

    // 애니메이션 WebP를 위한 ImageLoader 설정
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                // AnimatedWebP 지원
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(Unit) {
        launch {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 5000,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animationProgress = value
            }
        }

        delay(10000)
        onTimeout()
    }

    val gifSize = 200.dp
    val startX = -(screenWidth.value / 2) + (gifSize.value / 2)
    val endX = (screenWidth.value / 2) - (gifSize.value / 2)
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

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(R.drawable.tonie)
                .crossfade(false)
                .memoryCachePolicy(coil.request.CachePolicy.DISABLED) // 캐시 비활성화로 애니메이션 재생 보장
                .build(),
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier
                .size(gifSize)
                .graphicsLayer {
                    translationX = gifOffsetX
                }
        )
    }
}