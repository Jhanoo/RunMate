package com.D107.runmate.watch.presentation.running

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun RunningScreen(pace: String = "0:00") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Running Screen",
                fontSize = 20.sp
            )
            Text(
                text = "Pace: $pace",
                fontSize = 24.sp
            )
        }
    }
}