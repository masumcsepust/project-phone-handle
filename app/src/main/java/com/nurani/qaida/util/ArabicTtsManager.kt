package com.nurani.qaida.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/**
 * Thin wrapper around [TextToSpeech] that prefers an Arabic voice.
 *
 * Falls back from ar-SA → ar → device default, and tells the user once
 * if no Arabic voice is installed so silence never looks like a bug.
 */
class ArabicTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val tts = TextToSpeech(appContext, this)
    private var ready = false
    private var arabicAvailable = false
    private var warnedMissingArabic = false
    private var queuedText: String? = null

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TextToSpeech init failed: $status")
            return
        }
        @Suppress("DEPRECATION") // Locale(String) is the classic form; kept for older TTS engines.
        val candidates = listOf(Locale.forLanguageTag("ar-SA"), Locale("ar"))
        arabicAvailable = candidates.any { locale ->
            val result = tts.setLanguage(locale)
            result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
        tts.setSpeechRate(0.8f) // Slightly slower: learners need to hear the makhraj.
        ready = true
        queuedText?.let { speak(it) }
        queuedText = null
    }

    /** Speaks [text]; calls made before the engine is ready are queued. */
    fun speak(text: String) {
        if (!ready) {
            queuedText = text
            return
        }
        if (!arabicAvailable && !warnedMissingArabic) {
            warnedMissingArabic = true
            Toast.makeText(
                appContext,
                "আরবি ভয়েস ইনস্টল নেই। সেটিংস > টেক্সট-টু-স্পিচ থেকে আরবি ভাষা যোগ করুন।",
                Toast.LENGTH_LONG,
            ).show()
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "qaida-${text.hashCode()}")
    }

    fun stop() {
        if (ready) tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val TAG = "ArabicTts"
    }
}

/** Provided once in MainActivity so any composable can play audio. */
val LocalTts = staticCompositionLocalOf<ArabicTtsManager?> { null }
