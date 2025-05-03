package com.D107.runmate.watch.presentation.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text

@Composable
fun MenuScreen() {
    Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
    ) {
        Text("Menu Screen")
    }
}