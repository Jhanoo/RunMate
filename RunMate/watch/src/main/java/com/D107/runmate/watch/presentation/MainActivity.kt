/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.D107.runmate.watch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.D107.runmate.watch.R
import com.D107.runmate.watch.presentation.menu.MenuScreen
import com.D107.runmate.watch.presentation.pace.PaceScreen
import com.D107.runmate.watch.presentation.running.RunningScreen
import com.D107.runmate.watch.presentation.splash.SplashScreen
import com.D107.runmate.watch.presentation.theme.RunMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { false }

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            var currentScreen by remember { mutableStateOf("splash") }

            RunMateTheme {
                when (currentScreen) {
                    "splash" -> SplashScreen(onTimeout = { currentScreen = "menu" })
                    "menu" -> MenuScreen(
                        onNavigateToRunning = { currentScreen = "running" },
                        onNavigateToPace = { currentScreen = "pace" },
                    )
                    "running" -> RunningScreen()
                    "pace" -> PaceScreen()
                }
            }
        }
    }
}

//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true, showBackground = true)
//@Composable
//fun ScreenPreview() {
//    RunMateTheme {
//        MenuScreen()
////        SplashScreen(onTimeout = {})
//    }
//}
