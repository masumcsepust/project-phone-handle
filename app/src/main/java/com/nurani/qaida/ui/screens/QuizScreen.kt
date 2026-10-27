package com.nurani.qaida.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.QuizQuestion
import com.nurani.qaida.ui.components.EmeraldBrush
import com.nurani.qaida.ui.components.GoldBrush
import com.nurani.qaida.ui.components.GradientCard
import com.nurani.qaida.ui.components.PillButton
import com.nurani.qaida.ui.components.QaidaProgressBar
import com.nurani.qaida.ui.components.QaidaTopBar
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.SpeakerButton
import com.nurani.qaida.ui.components.TrophyIllustration
import com.nurani.qaida.ui.viewmodel.QuizState
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.CardBorder
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary

/** Letter-recognition quiz: see an Arabic letter, pick its Bengali name. */
@Composable
fun QuizScreen(
    quiz: QuizState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        if (quiz.questions.isEmpty() || quiz.finished) onStart()
    }
    ScreenBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            QaidaTopBar(title = "কুইজ ও পরীক্ষা", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val question = quiz.current
                when {
                    quiz.finished -> QuizResult(quiz, onRestart = onStart, onBack = onBack)
                    question == null -> CircularProgressIndicator(
                        Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                        color = Primary,
                    )
                    else -> QuizQuestionContent(quiz, question, onAnswer, onNext, onSpeak)
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionContent(
    quiz: QuizState,
    question: QuizQuestion,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    Text(
        "প্রশ্ন ${(quiz.currentIndex + 1).toBengaliDigits()}/${quiz.questions.size.toBengaliDigits()}",
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
    )
    QaidaProgressBar(progress = (quiz.currentIndex + 1f) / quiz.questions.size)
    GradientCard(brush = EmeraldBrush, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("এই হরফটির নাম কী?", style = MaterialTheme.typography.titleMedium, color = SecondaryLight)
            Text(question.promptLetter.letter, fontSize = 88.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
            SpeakerButton(
                onClick = { onSpeak(question.promptLetter.arabicName) },
                containerColor = SurfaceWhite,
                iconTint = Primary,
                contentDescription = "হরফটি শুনুন",
            )
        }
    }
    val answered = quiz.selectedOptionId != null
    question.options.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { option ->
                val isCorrect = option.id == question.correctId
                val isChosen = option.id == quiz.selectedOptionId
                val bg by animateColorAsState(
                    when {
                        answered && isCorrect -> PrimaryLight
                        answered && isChosen -> SecondaryDark.copy(alpha = 0.85f)
                        else -> SurfaceWhite
                    },
                    label = "optionBg",
                )
                Text(
                    option.nameBn,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (answered && (isCorrect || isChosen)) PrimaryDark else TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable(enabled = !answered, role = Role.Button) { onAnswer(option.id) }
                        .padding(vertical = 18.dp),
                )
            }
        }
    }
    if (answered) {
        val correct = quiz.selectedOptionId == question.correctId
        Text(
            if (correct) "সঠিক! মাশাআল্লাহ 🌟" else "সঠিক উত্তর: ${question.promptLetter.nameBn}",
            style = MaterialTheme.typography.titleMedium,
            color = if (correct) Primary else SecondaryDark,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        PillButton(
            text = if (quiz.currentIndex == quiz.questions.lastIndex) "ফলাফল দেখুন" else "পরবর্তী প্রশ্ন",
            onClick = onNext,
            trailingIcon = Icons.Filled.PlayArrow,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizResult(quiz: QuizState, onRestart: () -> Unit, onBack: () -> Unit) {
    GradientCard(brush = EmeraldBrush, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TrophyIllustration(size = 110.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                "${quiz.correctCount.toBengaliDigits()}/${quiz.questions.size.toBengaliDigits()} সঠিক",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SurfaceWhite,
            )
            Text(
                "+${quiz.pointsEarned.toBengaliDigits()} কুইজ পয়েন্ট",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryLight,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (quiz.correctCount == quiz.questions.size) "অসাধারণ! সবগুলো সঠিক হয়েছে।"
                else "ভালো চেষ্টা! আবার খেলে আরও শিখুন।",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PillButton(
            text = "আবার খেলুন",
            onClick = onRestart,
            containerBrush = GoldBrush,
            contentColor = PrimaryDark,
            leadingIcon = Icons.Filled.Refresh,
            modifier = Modifier.weight(1f),
        )
        PillButton(
            text = "হোমে ফিরুন",
            onClick = onBack,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.size(1.dp))
}
