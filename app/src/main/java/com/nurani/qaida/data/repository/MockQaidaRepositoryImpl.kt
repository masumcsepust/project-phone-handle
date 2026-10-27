package com.nurani.qaida.data.repository

import com.nurani.qaida.data.model.Achievement
import com.nurani.qaida.data.model.AiEvaluation
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.data.model.ExampleWord
import com.nurani.qaida.data.model.HujurFeedback
import com.nurani.qaida.data.model.HujurSubmission
import com.nurani.qaida.data.model.LetterPosition
import com.nurani.qaida.data.model.PositionalForm
import com.nurani.qaida.data.model.RecentActivity
import com.nurani.qaida.data.model.RecordedAudio
import com.nurani.qaida.data.model.SubmissionStatus
import com.nurani.qaida.data.model.TATWEEL
import com.nurani.qaida.data.model.Ustad
import com.nurani.qaida.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * In-memory repository that simulates network latency with [delay].
 *
 * Submissions sent to the Hujur are "reviewed" automatically after
 * [reviewDelayMillis], so the full feedback loop can be tried offline.
 */
class MockQaidaRepositoryImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val random: Random = Random.Default,
    private val networkDelayMillis: Long = 600L,
    private val reviewDelayMillis: Long = 8_000L,
) : QaidaRepository {

    private val letters: List<ArabicLetter> = buildLetters()

    private val ustad = Ustad(id = "ustad-1", nameBn = "ওস্তাদ মোশাররফ", isVerified = true)

    private val latestFeedback = MutableStateFlow<HujurFeedback?>(
        HujurFeedback(
            submissionId = "seed-1",
            letterId = 2,
            ustad = ustad,
            scorePercent = 85,
            studentAudioSeconds = 4,
            ustadAudioSeconds = 6,
            improvementsBn = listOf(
                "মাখরাজ ও উচ্চারণ আরও স্পষ্ট করুন",
                "হরফের শেষের টানটি একটু বাড়ান",
            ),
            isUstadAudioHeard = true,
        )
    )

    private val pending = MutableStateFlow<List<HujurSubmission>>(emptyList())

    private val profile = MutableStateFlow(
        UserProfile(
            nameBn = "আহমেদ হাসান",
            level = 3,
            qaidaCompletionPercent = 25,
            dailyGoalPercent = 35,
            lettersLearned = 12,
            totalLetters = letters.size,
            aiPracticeCount = 18,
            ustadReviewCount = 5,
            quizPoints = 340,
            achievements = listOf(
                Achievement("first-step", "প্রথম ধাপ", unlocked = true),
                Achievement("best-speaker", "সেরা বক্তা", unlocked = true),
                Achievement("regular", "নিয়মিত", unlocked = true),
                Achievement("quiz-champ", "কুইজ চ্যাম্পিয়ন", unlocked = false),
            ),
            recentActivity = listOf(
                RecentActivity(letterId = 2, scorePercent = 85, verifiedByUstad = true, audioSeconds = 3),
            ),
        )
    )

    override fun getLetters(): Flow<List<ArabicLetter>> = flow {
        // TODO: Replace with Retrofit/Ktor API (or ship as bundled JSON/Room) — GET /v1/qaida/letters
        delay(networkDelayMillis / 2)
        emit(letters)
    }

    override suspend fun evaluatePronunciationWithAi(letterId: Int, audio: RecordedAudio): AiEvaluation {
        // TODO: Replace with Retrofit/Ktor API — POST /v1/pronunciation/evaluate (multipart audio upload)
        delay(networkDelayMillis * 2)
        val score = random.nextInt(from = 72, until = 98)
        val (message, tips) = when {
            score >= 90 -> "চমৎকার! আপনার উচ্চারণ অনেক ভালো হয়েছে।" to emptyList()
            score >= 80 -> "ভালো হয়েছে! আরেকটু স্পষ্ট করে বলুন।" to listOf("মাখরাজ থেকে আওয়াজ বের করুন")
            else -> "আবার চেষ্টা করুন, আপনি পারবেন!" to listOf(
                "ধীরে ধীরে উচ্চারণ করুন",
                "ওস্তাদের অডিও শুনে অনুসরণ করুন",
            )
        }
        profile.update { it.copy(aiPracticeCount = it.aiPracticeCount + 1) }
        return AiEvaluation(letterId = letterId, scorePercent = score, messageBn = message, tipsBn = tips)
    }

    override suspend fun submitAudioToHujur(letterId: Int, audio: RecordedAudio): HujurSubmission {
        // TODO: Replace with Retrofit/Ktor API — POST /v1/hujur/submissions (multipart audio upload)
        delay(networkDelayMillis)
        val submission = HujurSubmission(
            id = "sub-${audio.recordedAtMillis}-${random.nextInt(1000)}",
            letterId = letterId,
            audio = audio,
            status = SubmissionStatus.PENDING_REVIEW,
            submittedAtMillis = System.currentTimeMillis(),
        )
        pending.update { it + submission }
        scope.launch { simulateUstadReview(submission) }
        return submission
    }

    override fun getPendingSubmissions(): Flow<List<HujurSubmission>> = pending.asStateFlow()

    override fun getHujurFeedback(): Flow<HujurFeedback?> {
        // TODO: Replace with Retrofit/Ktor API — GET /v1/hujur/feedback/latest (or push via FCM/WebSocket)
        return latestFeedback.asStateFlow()
    }

    override suspend fun markUstadAudioHeard(submissionId: String) {
        // TODO: Replace with Retrofit/Ktor API — PATCH /v1/hujur/feedback/{id} { heard: true }
        latestFeedback.update { fb ->
            if (fb?.submissionId == submissionId) fb.copy(isUstadAudioHeard = true) else fb
        }
    }

    override fun getUserProfile(): Flow<UserProfile> {
        // TODO: Replace with Retrofit/Ktor API — GET /v1/me/profile
        return profile.asStateFlow()
    }

    override suspend fun addQuizPoints(points: Int) {
        // TODO: Replace with Retrofit/Ktor API — POST /v1/me/quiz-results
        delay(networkDelayMillis / 3)
        profile.update { it.copy(quizPoints = it.quizPoints + points) }
    }

    private suspend fun simulateUstadReview(submission: HujurSubmission) {
        delay(reviewDelayMillis)
        val score = random.nextInt(from = 70, until = 96)
        val improvements = buildList {
            add("মাখরাজ ও উচ্চারণ আরও স্পষ্ট করুন")
            if (score < 85) add("হরফের শেষের টানটি একটু বাড়ান")
            if (score < 78) add("জোর দিয়ে নয়, স্বাভাবিকভাবে পড়ুন")
        }
        latestFeedback.value = HujurFeedback(
            submissionId = submission.id,
            letterId = submission.letterId,
            ustad = ustad,
            scorePercent = score,
            studentAudioSeconds = submission.audio.durationSeconds,
            ustadAudioSeconds = 5,
            improvementsBn = improvements,
            isUstadAudioHeard = false,
        )
        pending.update { list -> list.filterNot { it.id == submission.id } }
        profile.update {
            it.copy(
                ustadReviewCount = it.ustadReviewCount + 1,
                recentActivity = (listOf(
                    RecentActivity(
                        letterId = submission.letterId,
                        scorePercent = score,
                        verifiedByUstad = true,
                        audioSeconds = submission.audio.durationSeconds,
                    )
                ) + it.recentActivity).take(5),
            )
        }
    }

    companion object {
        private data class LetterSeed(
            val letter: String,
            val arabicName: String,
            val nameBn: String,
            val translit: String,
            val joinsToNext: Boolean = true,
            val initial: ExampleWord? = null,
            val medial: ExampleWord? = null,
            val final: ExampleWord? = null,
        )

        private val SEEDS = listOf(
            LetterSeed(
                "ا", "ألف", "আলিফ", "Alif", joinsToNext = false,
                initial = ExampleWord("", "أَ", "بٌ", "পিতা"),
                medial = ExampleWord("بَ", "ا", "بٌ", "দরজা"),
                final = ExampleWord("عَصَ", "ا", "", "লাঠি"),
            ),
            LetterSeed(
                "ب", "باء", "বা", "Ba",
                initial = ExampleWord("", "بَ", "يْتٌ", "ঘর"),
                medial = ExampleWord("سَ", "بَ", "قَ", "আগে গেল"),
                final = ExampleWord("كَتَ", "بَ", "", "লিখল"),
            ),
            LetterSeed(
                "ت", "تاء", "তা", "Ta",
                initial = ExampleWord("", "تَ", "مْرٌ", "খেজুর"),
                medial = ExampleWord("كِ", "تَ", "ابٌ", "বই"),
                final = ExampleWord("بَيْ", "تٌ", "", "ঘর"),
            ),
            LetterSeed(
                "ث", "ثاء", "ছা", "Tha",
                initial = ExampleWord("", "ثَ", "وْبٌ", "কাপড়"),
                medial = ExampleWord("مِ", "ثْ", "لُ", "মতো"),
                final = ExampleWord("حَدِي", "ثٌ", "", "হাদিস"),
            ),
            LetterSeed(
                "ج", "جيم", "জীম", "Jeem",
                initial = ExampleWord("", "جَ", "مَلٌ", "উট"),
                medial = ExampleWord("مَسْ", "جِ", "دٌ", "মসজিদ"),
                final = ExampleWord("ثَلْ", "جٌ", "", "বরফ"),
            ),
            LetterSeed(
                "ح", "حاء", "হা", "Ha",
                initial = ExampleWord("", "حَ", "جَرٌ", "পাথর"),
                medial = ExampleWord("رَ", "حْ", "مَةٌ", "রহমত"),
                final = ExampleWord("مِلْ", "حٌ", "", "লবণ"),
            ),
            LetterSeed(
                "خ", "خاء", "খ", "Kha",
                initial = ExampleWord("", "خُ", "بْزٌ", "রুটি"),
                medial = ExampleWord("نَ", "خْ", "لٌ", "খেজুর গাছ"),
                final = ExampleWord("شَيْ", "خٌ", "", "শায়খ"),
            ),
            LetterSeed("د", "دال", "দাল", "Dal", joinsToNext = false),
            LetterSeed("ذ", "ذال", "যাল", "Dhal", joinsToNext = false),
            LetterSeed("ر", "راء", "রা", "Ra", joinsToNext = false),
            LetterSeed("ز", "زاي", "যা", "Zay", joinsToNext = false),
            LetterSeed("س", "سين", "সীন", "Seen"),
            LetterSeed("ش", "شين", "শীন", "Sheen"),
            LetterSeed("ص", "صاد", "সোয়াদ", "Saad"),
            LetterSeed("ض", "ضاد", "দোয়াদ", "Daad"),
            LetterSeed("ط", "طاء", "ত্বোয়া", "Taa"),
            LetterSeed("ظ", "ظاء", "যোয়া", "Zaa"),
            LetterSeed("ع", "عين", "আইন", "Ain"),
            LetterSeed("غ", "غين", "গাইন", "Ghain"),
            LetterSeed("ف", "فاء", "ফা", "Fa"),
            LetterSeed("ق", "قاف", "ক্বাফ", "Qaf"),
            LetterSeed("ك", "كاف", "কাফ", "Kaf"),
            LetterSeed("ل", "لام", "লাম", "Lam"),
            LetterSeed("م", "ميم", "মীম", "Meem"),
            LetterSeed("ن", "نون", "নূন", "Noon"),
            LetterSeed("و", "واو", "ওয়াও", "Waw", joinsToNext = false),
            LetterSeed("ه", "هاء", "হা", "Ha (Haa)"),
            LetterSeed("ء", "همزة", "হামযা", "Hamza", joinsToNext = false),
            LetterSeed("ي", "ياء", "ইয়া", "Ya"),
        )

        /** Builds the 29 letters, deriving positional forms from joining rules. */
        fun buildLetters(): List<ArabicLetter> = SEEDS.mapIndexed { index, seed ->
            val l = seed.letter
            val isHamza = l == "ء"
            val initialForm = when {
                isHamza || !seed.joinsToNext -> l
                else -> l + TATWEEL
            }
            val medialForm = when {
                isHamza -> l
                seed.joinsToNext -> TATWEEL + l + TATWEEL
                else -> TATWEEL + l
            }
            val finalForm = if (isHamza) l else TATWEEL + l
            ArabicLetter(
                id = index + 1,
                letter = l,
                arabicName = seed.arabicName,
                nameBn = seed.nameBn,
                transliteration = seed.translit,
                joinsToNext = seed.joinsToNext,
                forms = listOf(
                    PositionalForm(LetterPosition.ISOLATED, l, null),
                    PositionalForm(LetterPosition.INITIAL, initialForm, seed.initial),
                    PositionalForm(LetterPosition.MEDIAL, medialForm, seed.medial),
                    PositionalForm(LetterPosition.FINAL, finalForm, seed.final),
                ),
            )
        }
    }
}
