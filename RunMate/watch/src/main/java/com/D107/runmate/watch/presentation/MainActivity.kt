package com.D107.runmate.watch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.D107.runmate.watch.presentation.menu.MenuScreen
import com.D107.runmate.watch.presentation.pace.PaceScreen
import com.D107.runmate.watch.presentation.running.DisplayMode
import com.D107.runmate.watch.presentation.running.PauseScreen
import com.D107.runmate.watch.presentation.running.ResultScreen
import com.D107.runmate.watch.presentation.running.RunningData
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
            RunMateTheme {
                val navController = rememberSwipeDismissableNavController()

                // RunningScreen의 상태를 저장할 변수들
                var savedTopIndex by remember { mutableStateOf(0) }
                var savedLeftIndex by remember { mutableStateOf(1) }
                var savedRightIndex by remember { mutableStateOf(2) }
                var savedPace by remember { mutableStateOf("0:00") }
                var savedRunningData by remember { mutableStateOf(RunningData()) }

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {

                    // 스플래시 화면에서 메뉴 화면으로 이동 (백스택x)
                    composable("splash") {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate("menu") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 메뉴 화면에서 러닝, 페이스 설정 화면으로 이동
                    composable("menu") {
                        MenuScreen(
                            onNavigateToRunning = {
                                navController.navigate("running") {
                                    popUpTo("menu") { inclusive = true }
                                }
                            },
                            onNavigateToPace = { navController.navigate("pace") }
                        )
                    }

                    // 시작 화면에서 페이스 설정 없이 시작하는 경우
                    composable("pace") {
                        PaceScreen(
                            onPaceSelected = { pace ->
                                savedPace = pace
                                navController.navigate("running/$pace") {
                                    popUpTo("menu") {inclusive = true}
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 페이스 설정 후 시작하는 경우
                    composable("running") {
                        RunningScreen(
                            savedState = Triple(savedTopIndex, savedLeftIndex, savedRightIndex),
                            onPauseClick = { mode, data, topIndex, leftIndex, rightIndex, currentRunningData ->
                                savedTopIndex = topIndex
                                savedLeftIndex = leftIndex
                                savedRightIndex = rightIndex
                                savedRunningData = currentRunningData

                                navController.navigate("pause/${mode.name}/$data") {
                                    popUpTo("running") {inclusive = true}
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // Running Screen (with pace)
                    composable("running/{pace}") { backStackEntry ->
                        val pace = backStackEntry.arguments?.getString("pace") ?: "0:00"
                        RunningScreen(
                            pace = pace,
                            runningData = savedRunningData,
                            savedState = Triple(savedTopIndex, savedLeftIndex, savedRightIndex),
                            onPauseClick = { mode, data, topIndex, leftIndex, rightIndex, currentRunningData ->
                                savedTopIndex = topIndex
                                savedLeftIndex = leftIndex
                                savedRightIndex = rightIndex
                                savedPace = pace
                                savedRunningData = currentRunningData

                                navController.navigate("pause/${mode.name}/$data") {
                                    popUpTo("running/{pace}") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 일시정지 화면에서 러닝,결과 화면으로 이동 (백스택x)
                    composable("pause/{mode}/{data}") { backStackEntry ->
                        val mode = DisplayMode.valueOf(
                            backStackEntry.arguments?.getString("mode") ?: "TIME"
                        )
                        val data = backStackEntry.arguments?.getString("data") ?: ""

                        PauseScreen(
                            displayMode = mode,
                            displayData = data,
                            onStartClick = {
                                // savedPace 값에 따라 올바른 running 화면으로 이동
                                if (savedPace != "0:00") {
                                    navController.navigate("running/$savedPace") {
                                        popUpTo("pause/{mode}/{data}") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate("running") {
                                        popUpTo("pause/{mode}/{data}") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onStopClick = {
                                // 상태 초기화
                                savedTopIndex = 0
                                savedLeftIndex = 1
                                savedRightIndex = 2
                                savedPace = "0:00"
                                savedRunningData = RunningData()

                                navController.navigate("result") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 결과 화면
                    composable("result") {
                        ResultScreen(
                            onClick = {
                                navController.navigate("menu") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true, showBackground = true)
@Composable
fun ScreenPreview() {
    RunMateTheme {
        PauseScreen()
    }
}