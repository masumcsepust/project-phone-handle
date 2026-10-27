package com.nurani.qaida.data.repository

import com.nurani.qaida.data.model.AiEvaluation
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.data.model.HujurFeedback
import com.nurani.qaida.data.model.HujurSubmission
import com.nurani.qaida.data.model.RecordedAudio
import com.nurani.qaida.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the app. The UI/ViewModel depend only on this
 * interface, so swapping [MockQaidaRepositoryImpl] for a network-backed
 * implementation needs no UI changes.
 */
interface QaidaRepository {

    /** All 29 Qaida letters, in teaching order. */
    fun getLetters(): Flow<List<ArabicLetter>>

    /** Scores a recorded attempt at [letterId]. */
    suspend fun evaluatePronunciationWithAi(letterId: Int, audio: RecordedAudio): AiEvaluation

    /** Sends a recorded attempt to an Ustad for manual review. */
    suspend fun submitAudioToHujur(letterId: Int, audio: RecordedAudio): HujurSubmission

    /** Submissions still waiting for an Ustad to listen. */
    fun getPendingSubmissions(): Flow<List<HujurSubmission>>

    /** The most recent reviewed feedback, or null when none exists yet. */
    fun getHujurFeedback(): Flow<HujurFeedback?>

    /** Marks the Ustad's audio reply as listened to. */
    suspend fun markUstadAudioHeard(submissionId: String)

    fun getUserProfile(): Flow<UserProfile>

    /** Adds quiz points earned in a finished quiz. */
    suspend fun addQuizPoints(points: Int)
}
