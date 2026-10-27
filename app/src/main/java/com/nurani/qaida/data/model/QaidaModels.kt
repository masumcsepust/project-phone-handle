package com.nurani.qaida.data.model

/** Arabic tatweel (kashida) used to show the joining stroke of positional forms. */
const val TATWEEL = "ـ"

/** The four shapes an Arabic letter takes depending on where it sits in a word. */
enum class LetterPosition(val titleBn: String, val titleEn: String, val stripBn: String) {
    ISOLATED("একক", "Isolated", "একাকী বসে"),
    INITIAL("শুরুতে", "Beginning", "শব্দের শুরুতে"),
    MEDIAL("মাঝখানে", "Middle", "শব্দের মাঝে"),
    FINAL("শেষে", "End", "শব্দের শেষে"),
}

/**
 * An example word split around the target letter so the UI can colour
 * only [target] (letter + its harakat) without breaking Arabic shaping.
 */
data class ExampleWord(
    val before: String,
    val target: String,
    val after: String,
    val meaningBn: String,
) {
    val full: String get() = before + target + after
}

data class PositionalForm(
    val position: LetterPosition,
    val form: String,
    val example: ExampleWord?,
)

data class ArabicLetter(
    val id: Int,
    val letter: String,
    /** Spelled-out Arabic name, e.g. "باء" — gives TTS a clearer utterance. */
    val arabicName: String,
    val nameBn: String,
    val transliteration: String,
    /** Letters like ا د ذ ر ز و never join to the letter after them. */
    val joinsToNext: Boolean,
    val forms: List<PositionalForm>,
) {
    fun form(position: LetterPosition): PositionalForm = forms.first { it.position == position }
}

/** Opaque handle to a recorded clip. Becomes a file/URI once real recording is added. */
data class RecordedAudio(
    val letterId: Int,
    val durationSeconds: Int,
    val recordedAtMillis: Long,
)

data class AiEvaluation(
    val letterId: Int,
    val scorePercent: Int,
    val messageBn: String,
    val tipsBn: List<String>,
)

enum class SubmissionStatus { PENDING_REVIEW, REVIEWED }

data class HujurSubmission(
    val id: String,
    val letterId: Int,
    val audio: RecordedAudio,
    val status: SubmissionStatus,
    val submittedAtMillis: Long,
)

data class Ustad(
    val id: String,
    val nameBn: String,
    val isVerified: Boolean,
)

data class HujurFeedback(
    val submissionId: String,
    val letterId: Int,
    val ustad: Ustad,
    val scorePercent: Int,
    val studentAudioSeconds: Int,
    val ustadAudioSeconds: Int,
    val improvementsBn: List<String>,
    val isUstadAudioHeard: Boolean,
)

data class Achievement(
    val id: String,
    val titleBn: String,
    val unlocked: Boolean,
)

data class RecentActivity(
    val letterId: Int,
    val scorePercent: Int,
    val verifiedByUstad: Boolean,
    val audioSeconds: Int,
)

data class UserProfile(
    val nameBn: String,
    val level: Int,
    val qaidaCompletionPercent: Int,
    val dailyGoalPercent: Int,
    val lettersLearned: Int,
    val totalLetters: Int,
    val aiPracticeCount: Int,
    val ustadReviewCount: Int,
    val quizPoints: Int,
    val achievements: List<Achievement>,
    val recentActivity: List<RecentActivity>,
)

data class QuizQuestion(
    val promptLetter: ArabicLetter,
    val options: List<ArabicLetter>,
) {
    val correctId: Int get() = promptLetter.id
}
