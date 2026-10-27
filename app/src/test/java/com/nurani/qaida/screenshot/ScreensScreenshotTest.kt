package com.nurani.qaida.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.nurani.qaida.data.model.AiEvaluation
import com.nurani.qaida.data.repository.MockQaidaRepositoryImpl
import com.nurani.qaida.ui.navigation.QaidaBottomBar
import com.nurani.qaida.ui.screens.AiAssistScreen
import com.nurani.qaida.ui.screens.HomeScreen
import com.nurani.qaida.ui.screens.HujurVerifyScreen
import com.nurani.qaida.ui.screens.LettersScreen
import com.nurani.qaida.ui.screens.ProfileScreen
import com.nurani.qaida.ui.theme.NuraniQaidaTheme
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.ui.viewmodel.RecordingPhase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every screen on the JVM. Run with
 * `./gradlew testDebugUnitTest -Proborazzi.record=true` to write PNGs
 * into app/build/outputs/roborazzi.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h1300dp-xhdpi")
class ScreensScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val state: QaidaUiState = runBlocking {
        val repo = MockQaidaRepositoryImpl(networkDelayMillis = 0)
        QaidaUiState(
            isLoading = false,
            letters = repo.getLetters().first(),
            hujurFeedback = repo.getHujurFeedback().first(),
            profile = repo.getUserProfile().first(),
        )
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        compose.setContent {
            NuraniQaidaTheme {
                Scaffold(bottomBar = { QaidaBottomBar(null, hujurBadgeCount = 1, onTabSelected = {}) }) { p ->
                    androidx.compose.foundation.layout.Box(Modifier.padding(bottom = p.calculateBottomPadding())) {
                        content()
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(2_000)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test
    fun home() = capture("1_home") {
        HomeScreen(state, {}, {}, {}, {}, {}, {})
    }

    @Test
    fun letters() = capture("2_letters") {
        LettersScreen(state, {}, {}, {}, {}, {}, {})
    }

    @Test
    fun aiAssist() = capture("3_ai_assist") {
        AiAssistScreen(
            state.copy(
                recordingPhase = RecordingPhase.DONE,
                aiEvaluation = AiEvaluation(2, 92, "চমৎকার! আপনার উচ্চারণ অনেক ভালো হয়েছে।", emptyList()),
            ),
            {}, {}, {}, {}, {},
        )
    }

    @Test
    fun hujur() = capture("4_hujur") {
        HujurVerifyScreen(state, {}, {}, {}, {})
    }

    @Test
    fun profile() = capture("5_profile") {
        ProfileScreen(state, {}, {})
    }
}
