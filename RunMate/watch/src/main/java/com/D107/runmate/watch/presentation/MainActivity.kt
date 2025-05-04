package com.D107.runmate.watch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.D107.runmate.watch.presentation.menu.MenuScreen
import com.D107.runmate.watch.presentation.pace.PaceScreen
import com.D107.runmate.watch.presentation.running.PauseScreen
import com.D107.runmate.watch.presentation.running.ResultScreen
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
                            onNavigateToRunning = { navController.navigate("running") },
                            onNavigateToPace = { navController.navigate("pace") }
                        )
                    }

                    // 시작 화면에서 페이스 설정 없이 시작하는 경우
                    composable("pace") {
                        PaceScreen(
                            onPaceSelected = { pace ->
                                navController.navigate("running/$pace")
                            }
                        )
                    }

                    // 페이스 설정 후 시작하는 경우
                    composable("running") {
                        RunningScreen(
                            onPauseClick = {
                                navController.navigate("pause") {
                                    popUpTo("running") { inclusive = false }
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
                            onPauseClick = {
                                navController.navigate("pause") {
                                    popUpTo("running/{pace}") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 일시정지 화면에서 러닝,결과 화면으로 이동 (백스택x)
                    composable("pause") {
                        PauseScreen(
                            onStartClick = {
                                navController.navigate("running") {
                                    popUpTo("pause") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onStopClick = {
                                navController.navigate("result") {
                                    popUpTo("menu") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 결과 화면
                    composable("result") {
                        ResultScreen()
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