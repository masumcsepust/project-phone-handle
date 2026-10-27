package com.nurani.qaida

import com.nurani.qaida.data.model.LetterPosition
import com.nurani.qaida.data.model.RecordedAudio
import com.nurani.qaida.data.model.TATWEEL
import com.nurani.qaida.data.repository.MockQaidaRepositoryImpl
import com.nurani.qaida.util.formatTimerBn
import com.nurani.qaida.util.toBengaliDigits
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class QaidaLogicTest {

    private val letters = MockQaidaRepositoryImpl.buildLetters()

    @Test
    fun `qaida has 29 letters with unique ids`() {
        assertEquals(29, letters.size)
        assertEquals(29, letters.map { it.id }.toSet().size)
    }

    @Test
    fun `connecting letter gets tatweel forms`() {
        val ba = letters.first { it.letter == "ب" }
        assertEquals("ب", ba.form(LetterPosition.ISOLATED).form)
        assertEquals("ب$TATWEEL", ba.form(LetterPosition.INITIAL).form)
        assertEquals("$TATWEEL" + "ب" + TATWEEL, ba.form(LetterPosition.MEDIAL).form)
        assertEquals("$TATWEEL" + "ب", ba.form(LetterPosition.FINAL).form)
    }

    @Test
    fun `non connecting letter never trails a tatweel`() {
        listOf("ا", "د", "ذ", "ر", "ز", "و").forEach { l ->
            val letter = letters.first { it.letter == l }
            assertFalse(letter.joinsToNext)
            letter.forms.forEach { assertFalse(it.form.endsWith(TATWEEL)) }
        }
    }

    @Test
    fun `first seven letters have full example words containing the target`() {
        letters.take(7).forEach { letter ->
            listOf(LetterPosition.INITIAL, LetterPosition.MEDIAL, LetterPosition.FINAL).forEach { pos ->
                val example = letter.form(pos).example
                assertNotNull("${letter.nameBn} $pos", example)
                // The highlighted target must begin with the letter itself (ignoring hamza seat on alif).
                val base = example!!.target.first().toString()
                assertTrue(base == letter.letter || (letter.letter == "ا" && base == "أ"))
            }
        }
    }

    @Test
    fun `bengali digits and timer`() {
        assertEquals("৮৫%", "85%".toBengaliDigits())
        assertEquals("১২/২৯", "12/29".toBengaliDigits())
        assertEquals("০০:০৩", formatTimerBn(3))
    }

    @Test
    fun `submission is reviewed after delay and updates profile`() = runTest {
        val repo = MockQaidaRepositoryImpl(
            scope = backgroundScope,
            random = Random(1),
            networkDelayMillis = 10,
            reviewDelayMillis = 1_000,
        )
        val before = repo.getUserProfile().first().ustadReviewCount
        val sub = repo.submitAudioToHujur(3, RecordedAudio(3, 3, 42L))
        assertEquals(1, repo.getPendingSubmissions().first().size)

        advanceTimeBy(1_500)

        assertTrue(repo.getPendingSubmissions().first().isEmpty())
        val feedback = repo.getHujurFeedback().first()!!
        assertEquals(sub.id, feedback.submissionId)
        assertFalse(feedback.isUstadAudioHeard)
        assertEquals(before + 1, repo.getUserProfile().first().ustadReviewCount)

        repo.markUstadAudioHeard(sub.id)
        assertTrue(repo.getHujurFeedback().first()!!.isUstadAudioHeard)
    }
}
