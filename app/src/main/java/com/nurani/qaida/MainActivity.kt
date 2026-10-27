package com.nurani.qaida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nurani.qaida.ui.navigation.QaidaBottomBar
import com.nurani.qaida.ui.navigation.Routes
import com.nurani.qaida.ui.navigation.navigateToTab
import com.nurani.qaida.ui.screens.AiAssistScreen
import com.nurani.qaida.ui.screens.HomeScreen
import com.nurani.qaida.ui.screens.HujurVerifyScreen
import com.nurani.qaida.ui.screens.LettersScreen
import com.nurani.qaida.ui.screens.ProfileScreen
import com.nurani.qaida.ui.screens.QuizScreen
import com.nurani.qaida.ui.theme.NuraniQaidaTheme
import com.nurani.qaida.ui.viewmodel.QaidaViewModel
import com.nurani.qaida.ui.viewmodel.UiEvent
import com.nurani.qaida.util.ArabicTtsManager
import com.nurani.qaida.util.LocalTts

class MainActivity : ComponentActivity() {

    private lateinit var tts: ArabicTtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = ArabicTtsManager(this)
        setContent {
            NuraniQaidaTheme {
                CompositionLocalProvider(LocalTts provides tts) {
                    QaidaApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        tts.stop()
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}

@Composable
fun QaidaApp(viewModel: QaidaViewModel = viewModel(factory = QaidaViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tts = LocalTts.current
    val speak: (String) -> Unit = { text -> tts?.speak(text) }

    LaunchedEffect(viewModel, navController) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                UiEvent.OpenAiPractice -> navController.navigateToTab(Routes.AI_ASSIST)
                UiEvent.OpenHujur -> navController.navigateToTab(Routes.HUJUR)
            }
        }
    }

    val goHome = { navController.navigateToTab(Routes.HOME) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            QaidaBottomBar(
                currentDestination = backStackEntry?.destination,
                hujurBadgeCount = state.hujurBadgeCount,
                onTabSelected = { tab -> navController.navigateToTab(tab.route) },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // Screens draw their own gradient under the status bar, so only the
            // bottom bar's height is applied here.
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    state = state,
                    onPracticeLetters = { navController.navigateToTab(Routes.LETTERS) },
                    onOpenAiAssist = { navController.navigateToTab(Routes.AI_ASSIST) },
                    onOpenHujur = { navController.navigateToTab(Routes.HUJUR) },
                    onOpenQuiz = { navController.navigate(Routes.QUIZ) { launchSingleTop = true } },
                    onOpenProfile = { navController.navigateToTab(Routes.PROFILE) },
                    onSpeak = speak,
                )
            }
            composable(Routes.LETTERS) {
                LettersScreen(
                    state = state,
                    onBack = goHome,
                    onSelectLetter = viewModel::selectLetter,
                    onNextLetter = viewModel::nextLetter,
                    onSpeak = speak,
                    onPracticeWithAi = {
                        viewModel.startRecording()
                        navController.navigateToTab(Routes.AI_ASSIST)
                    },
                    onSendToHujur = viewModel::submitToHujur,
                )
            }
            composable(Routes.AI_ASSIST) {
                AiAssistScreen(
                    state = state,
                    onBack = goHome,
                    onSpeak = speak,
                    onRecord = viewModel::startRecording,
                    onStopRecording = viewModel::cancelRecording,
                    onSendToHujur = viewModel::submitToHujur,
                )
            }
            composable(Routes.HUJUR) {
                HujurVerifyScreen(
                    state = state,
                    onSpeak = speak,
                    onRecordAgain = {
                        viewModel.focusFeedbackLetter()
                        navController.navigateToTab(Routes.AI_ASSIST)
                    },
                    onNextLesson = {
                        viewModel.focusFeedbackLetter()
                        viewModel.nextLetter()
                        navController.navigateToTab(Routes.LETTERS)
                    },
                    onUstadAudioPlayed = viewModel::markFeedbackHeard,
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    state = state,
                    onSpeak = speak,
                    onMenuClick = { label ->
                        viewModel.showMessage("'$label' শীঘ্রই আসছে, ইনশাআল্লাহ।")
                    },
                )
            }
            composable(Routes.QUIZ) {
                QuizScreen(
                    quiz = state.quiz,
                    onBack = { navController.popBackStack() },
                    onStart = viewModel::startQuiz,
                    onAnswer = viewModel::answerQuiz,
                    onNext = viewModel::nextQuizQuestion,
                    onSpeak = speak,
                )
            }
        }
    }
}
