package com.D107.runmate.watch.presentation.running

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.D107.runmate.watch.presentation.theme.RunMateTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class RunningScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var viewModel: RunningViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock ViewModel state
        `when`(viewModel.heartRate).thenReturn(MutableStateFlow(0))
    }

    @Test
    fun running_screen_displays_correctly() {
        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(viewModel = viewModel)
            }
        }

        // Check if basic elements are displayed
        composeTestRule.onNodeWithText("시간").assertExists()
        composeTestRule.onNodeWithText("BPM").assertExists()
        composeTestRule.onNodeWithText("km").assertExists()
    }

    @Test
    fun heart_rate_updates_display_correctly() {
        val heartRateFlow = MutableStateFlow(75)
        `when`(viewModel.heartRate).thenReturn(heartRateFlow)

        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(viewModel = viewModel)
            }
        }

        // Check initial heart rate
        composeTestRule.onNodeWithText("75").assertExists()

        // Update heart rate
        heartRateFlow.value = 85
        composeTestRule.waitForIdle()

        // Check updated heart rate
        composeTestRule.onNodeWithText("85").assertExists()
    }

    @Test
    fun display_buttons_are_clickable() {
        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(viewModel = viewModel)
            }
        }

        // Top display should be clickable
        composeTestRule.onNodeWithText("1:10:13").assertExists()
        composeTestRule.onNodeWithText("1:10:13").performClick()
    }

    @Test
    fun pause_button_is_clickable() {
        var pauseClicked = false

        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(
                    viewModel = viewModel,
                    onPauseClick = { _, _, _, _, _, _ ->
                        pauseClicked = true
                    }
                )
            }
        }

        // Find and click pause button (by content description)
        composeTestRule.onNodeWithContentDescription("Pause").performClick()

        // Verify pause was clicked
        assert(pauseClicked)
    }

    @Test
    fun custom_pace_displays_correctly() {
        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(
                    pace = "5:30",
                    viewModel = viewModel
                )
            }
        }

        // Check if pace is formatted and displayed correctly
        composeTestRule.onNodeWithText("5'30\"").assertExists()
    }

    @Test
    fun display_modes_cycle_correctly() {
        composeTestRule.setContent {
            RunMateTheme {
                RunningScreen(viewModel = viewModel)
            }
        }

        // Find the top display and click multiple times
        val topDisplay = composeTestRule.onNodeWithText("1:10:13")
        topDisplay.assertExists()

        // Click to cycle through modes
        topDisplay.performClick()
        composeTestRule.onNodeWithText("0").assertExists() // BPM

        topDisplay.performClick()
        composeTestRule.onNodeWithText("5'10\"").assertExists() // Pace

        topDisplay.performClick()
        composeTestRule.onNodeWithText("8.5").assertExists() // Distance

        topDisplay.performClick()
        composeTestRule.onNodeWithText("1:10:13").assertExists() // Back to Time
    }
}