package com.nurani.qaida.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nurani.qaida.data.model.AiEvaluation
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.data.model.HujurFeedback
import com.nurani.qaida.data.model.HujurSubmission
import com.nurani.qaida.data.model.QuizQuestion
import com.nurani.qaida.data.model.RecordedAudio
import com.nurani.qaida.data.model.UserProfile
import com.nurani.qaida.data.repository.MockQaidaRepositoryImpl
import com.nurani.qaida.data.repository.QaidaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RecordingPhase { IDLE, RECORDING, EVALUATING, DONE }

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionId: Int? = null,
    val correctCount: Int = 0,
    val finished: Boolean = false,
) {
    val current: QuizQuestion? get() = questions.getOrNull(currentIndex)
    val pointsEarned: Int get() = correctCount * POINTS_PER_ANSWER

    companion object {
        const val QUESTION_COUNT = 5
        const val POINTS_PER_ANSWER = 10
    }
}

data class QaidaUiState(
    val isLoading: Boolean = true,
    val letters: List<ArabicLetter> = emptyList(),
    val selectedIndex: Int = 1, // ب is the first letter taught with all four forms.
    val recordingPhase: RecordingPhase = RecordingPhase.IDLE,
    val secondsLeft: Int = RECORDING_SECONDS,
    val aiEvaluation: AiEvaluation? = null,
    val lastRecording: RecordedAudio? = null,
    val isSubmittingToHujur: Boolean = false,
    val pendingSubmissions: List<HujurSubmission> = emptyList(),
    val hujurFeedback: HujurFeedback? = null,
    val profile: UserProfile? = null,
    val quiz: QuizState = QuizState(),
) {
    val selectedLetter: ArabicLetter? get() = letters.getOrNull(selectedIndex)
    val isBusyRecording: Boolean
        get() = recordingPhase == RecordingPhase.RECORDING || recordingPhase == RecordingPhase.EVALUATING

    /** Badge count on the Hujur tab: unheard feedback + submissions in review. */
    val hujurBadgeCount: Int
        get() = pendingSubmissions.size + if (hujurFeedback?.isUstadAudioHeard == false) 1 else 0

    fun letterById(id: Int): ArabicLetter? = letters.firstOrNull { it.id == id }

    companion object {
        const val RECORDING_SECONDS = 3
    }
}

sealed interface UiEvent {
    data class ShowMessage(val text: String) : UiEvent
    data object OpenAiPractice : UiEvent
    data object OpenHujur : UiEvent
}

class QaidaViewModel(
    private val repository: QaidaRepository,
    private val random: Random = Random.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QaidaUiState())
    val uiState: StateFlow<QaidaUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private var recordingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getLetters().collect { letters ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        letters = letters,
                        selectedIndex = it.selectedIndex.coerceIn(0, (letters.size - 1).coerceAtLeast(0)),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getHujurFeedback().collect { fb -> _uiState.update { it.copy(hujurFeedback = fb) } }
        }
        viewModelScope.launch {
            repository.getPendingSubmissions().collect { list -> _uiState.update { it.copy(pendingSubmissions = list) } }
        }
        viewModelScope.launch {
            repository.getUserProfile().collect { p -> _uiState.update { it.copy(profile = p) } }
        }
    }

    // region Letters

    fun selectLetter(index: Int) {
        val letters = _uiState.value.letters
        if (index !in letters.indices || index == _uiState.value.selectedIndex) return
        cancelRecording()
        _uiState.update {
            it.copy(
                selectedIndex = index,
                aiEvaluation = null,
                lastRecording = null,
                recordingPhase = RecordingPhase.IDLE,
                secondsLeft = QaidaUiState.RECORDING_SECONDS,
            )
        }
    }

    fun nextLetter() {
        val state = _uiState.value
        if (state.letters.isEmpty()) return
        if (state.selectedIndex >= state.letters.lastIndex) {
            emit(UiEvent.ShowMessage("মাশাআল্লাহ! আপনি সব হরফ শেষ করেছেন।"))
            selectLetter(0)
        } else {
            selectLetter(state.selectedIndex + 1)
        }
    }

    fun previousLetter() = selectLetter(_uiState.value.selectedIndex - 1)

    // endregion

    // region Recording + AI evaluation

    /**
     * Simulates a 3-second recording, then asks the repository for an AI score.
     * Real capture (MediaRecorder + RECORD_AUDIO permission) would replace the countdown.
     */
    fun startRecording() {
        val state = _uiState.value
        val letter = state.selectedLetter ?: return
        if (state.isBusyRecording) return

        recordingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recordingPhase = RecordingPhase.RECORDING,
                    secondsLeft = QaidaUiState.RECORDING_SECONDS,
                    aiEvaluation = null,
                )
            }
            for (s in QaidaUiState.RECORDING_SECONDS downTo 1) {
                _uiState.update { it.copy(secondsLeft = s) }
                delay(1_000)
            }
            val audio = RecordedAudio(
                letterId = letter.id,
                durationSeconds = QaidaUiState.RECORDING_SECONDS,
                recordedAtMillis = System.currentTimeMillis(),
            )
            _uiState.update {
                it.copy(recordingPhase = RecordingPhase.EVALUATING, secondsLeft = 0, lastRecording = audio)
            }
            val result = runCatching { repository.evaluatePronunciationWithAi(letter.id, audio) }
            result.onSuccess { eval ->
                _uiState.update { it.copy(recordingPhase = RecordingPhase.DONE, aiEvaluation = eval) }
            }.onFailure {
                _uiState.update { it.copy(recordingPhase = RecordingPhase.IDLE) }
                emit(UiEvent.ShowMessage("মূল্যায়ন করা যায়নি। ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন।"))
            }
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        if (_uiState.value.isBusyRecording) {
            _uiState.update {
                it.copy(recordingPhase = RecordingPhase.IDLE, secondsLeft = QaidaUiState.RECORDING_SECONDS)
            }
        }
    }

    // endregion

    // region Hujur

    /**
     * Sends the latest recording of the selected letter to the Ustad.
     * With nothing recorded yet, guides the learner to the AI screen first.
     */
    fun submitToHujur() {
        val state = _uiState.value
        val letter = state.selectedLetter ?: return
        if (state.isSubmittingToHujur) return
        val audio = state.lastRecording?.takeIf { it.letterId == letter.id }
        if (audio == null) {
            emit(UiEvent.ShowMessage("প্রথমে মাইক চেপে '${letter.nameBn}' উচ্চারণ রেকর্ড করুন।"))
            emit(UiEvent.OpenAiPractice)
            return
        }
        if (state.pendingSubmissions.any { it.audio == audio }) {
            emit(UiEvent.OpenHujur)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingToHujur = true) }
            runCatching { repository.submitAudioToHujur(letter.id, audio) }
                .onSuccess {
                    emit(UiEvent.ShowMessage("আপনার পড়া ওস্তাদের কাছে পাঠানো হয়েছে। শীঘ্রই ফিডব্যাক পাবেন।"))
                    emit(UiEvent.OpenHujur)
                }
                .onFailure { emit(UiEvent.ShowMessage("পাঠানো যায়নি। আবার চেষ্টা করুন।")) }
            _uiState.update { it.copy(isSubmittingToHujur = false) }
        }
    }

    fun markFeedbackHeard() {
        val fb = _uiState.value.hujurFeedback ?: return
        if (fb.isUstadAudioHeard) return
        viewModelScope.launch { repository.markUstadAudioHeard(fb.submissionId) }
    }

    /** Selects the letter the latest feedback was about (for "retry" flows). */
    fun focusFeedbackLetter() {
        val fb = _uiState.value.hujurFeedback ?: return
        val index = _uiState.value.letters.indexOfFirst { it.id == fb.letterId }
        if (index >= 0) selectLetter(index)
    }

    // endregion

    // region Quiz

    fun startQuiz() {
        val letters = _uiState.value.letters
        if (letters.size < 4) return
        val prompts = letters.shuffled(random).take(QuizState.QUESTION_COUNT)
        val questions = prompts.map { prompt ->
            val distractors = letters.filter { it.id != prompt.id }.shuffled(random).take(3)
            QuizQuestion(promptLetter = prompt, options = (distractors + prompt).shuffled(random))
        }
        _uiState.update { it.copy(quiz = QuizState(questions = questions)) }
    }

    fun answerQuiz(optionId: Int) {
        val quiz = _uiState.value.quiz
        val question = quiz.current ?: return
        if (quiz.selectedOptionId != null || quiz.finished) return
        val correct = optionId == question.correctId
        _uiState.update {
            it.copy(
                quiz = quiz.copy(
                    selectedOptionId = optionId,
                    correctCount = quiz.correctCount + if (correct) 1 else 0,
                )
            )
        }
    }

    fun nextQuizQuestion() {
        val quiz = _uiState.value.quiz
        if (quiz.selectedOptionId == null) return
        if (quiz.currentIndex >= quiz.questions.lastIndex) {
            _uiState.update { it.copy(quiz = quiz.copy(finished = true)) }
            if (quiz.pointsEarned > 0) {
                viewModelScope.launch { runCatching { repository.addQuizPoints(quiz.pointsEarned) } }
            }
        } else {
            _uiState.update {
                it.copy(quiz = quiz.copy(currentIndex = quiz.currentIndex + 1, selectedOptionId = null))
            }
        }
    }

    // endregion

    fun showMessage(text: String) = emit(UiEvent.ShowMessage(text))

    private fun emit(event: UiEvent) {
        _events.trySend(event)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { QaidaViewModel(repository = MockQaidaRepositoryImpl()) }
        }
    }
}
