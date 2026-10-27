package com.nurani.qaida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.data.model.LetterPosition
import com.nurani.qaida.ui.components.AiRobotMascot
import com.nurani.qaida.ui.components.AvatarKind
import com.nurani.qaida.ui.components.EmeraldBrush
import com.nurani.qaida.ui.components.GoldBrush
import com.nurani.qaida.ui.components.GradientCard
import com.nurani.qaida.ui.components.MosqueSilhouette
import com.nurani.qaida.ui.components.PersonAvatar
import com.nurani.qaida.ui.components.PillButton
import com.nurani.qaida.ui.components.QaidaBookIllustration
import com.nurani.qaida.ui.components.QaidaCard
import com.nurani.qaida.ui.components.QaidaProgressBar
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.TealBrush
import com.nurani.qaida.ui.components.TrophyIllustration
import com.nurani.qaida.ui.components.WaveformBars
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.DividerGray
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryContainer
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.Secondary
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary
import com.nurani.qaida.ui.theme.TextSecondary
import com.nurani.qaida.ui.theme.WaveformColors

@Composable
fun HomeScreen(
    state: QaidaUiState,
    onPracticeLetters: () -> Unit,
    onOpenAiAssist: () -> Unit,
    onOpenHujur: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenProfile: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    ScreenBackground {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeHeader(onAvatarClick = onOpenProfile)
            DailyProgressCard(percent = state.profile?.dailyGoalPercent ?: 0)
            state.selectedLetter?.let { letter ->
                LetterHeroCard(letter = letter, onPractice = onPracticeLetters, onSpeak = onSpeak)
            }
            HujurHeroCard(state = state, onOpen = onOpenHujur, onSpeak = onSpeak)
            AiVoiceCard(onOpen = onOpenAiAssist)
            QuizCard(onOpen = onOpenQuiz)
        }
    }
}

@Composable
private fun HomeHeader(onAvatarClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(72.dp)) {
        MosqueSilhouette(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 56.dp)
                .size(width = 110.dp, height = 64.dp)
        )
        Row(Modifier.fillMaxWidth().align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "আসসালামু আলাইকুম!",
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryDark,
                modifier = Modifier.weight(1f),
            )
            PersonAvatar(
                kind = AvatarKind.USER,
                size = 52.dp,
                modifier = Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onAvatarClick),
            )
        }
    }
}

@Composable
private fun DailyProgressCard(percent: Int) {
    QaidaCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "আমার অগ্রগতি",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Daily goal ${percent.toBengaliDigits()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(10.dp))
            QaidaProgressBar(progress = percent / 100f)
        }
    }
}

@Composable
private fun LetterHeroCard(letter: ArabicLetter, onPractice: () -> Unit, onSpeak: (String) -> Unit) {
    GradientCard(brush = EmeraldBrush, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(
                        "আরবি হরফ ও অবস্থান\n(শুরু, মাঝে, শেষ)",
                        style = MaterialTheme.typography.titleLarge,
                        color = SurfaceWhite,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "হরফের অবস্থান শিখুন",
                        style = MaterialTheme.typography.titleSmall,
                        color = SecondaryLight,
                    )
                }
                QaidaBookIllustration(size = 72.dp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = letter.letter,
                    style = TextStyle(
                        brush = Brush.verticalGradient(listOf(SecondaryLight, SecondaryDark, Secondary)),
                        shadow = Shadow(Color(0x99000000), Offset(4f, 6f), blurRadius = 8f),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button) { onSpeak(letter.arabicName) }
                        .padding(horizontal = 8.dp),
                )
                Text("➔", color = SecondaryLight, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    listOf(
                        "একক:" to LetterPosition.ISOLATED,
                        "শুরুতে:" to LetterPosition.INITIAL,
                        "মাঝে:" to LetterPosition.MEDIAL,
                        "শেষে:" to LetterPosition.FINAL,
                    ).forEach { (label, pos) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, color = SurfaceWhite, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(letter.form(pos).form, color = Secondary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            PillButton(
                text = "অনুশীলন করুন",
                onClick = onPractice,
                containerColor = PrimaryLight,
                contentColor = PrimaryDark,
                trailingIcon = Icons.Filled.PlayArrow,
            )
        }
    }
}

@Composable
private fun HujurHeroCard(state: QaidaUiState, onOpen: () -> Unit, onSpeak: (String) -> Unit) {
    val feedback = state.hujurFeedback
    val pendingCount = state.pendingSubmissions.size
    Box(Modifier.fillMaxWidth()) {
        GradientCard(brush = TealBrush, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("ওস্তাদ বা হুজুর যাচাই", style = MaterialTheme.typography.titleLarge, color = SurfaceWhite)
                        Text(
                            "ভয়েস পাঠিয়ে সরাসরি ওস্তাদ থেকে ফিডব্যাক নিন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SurfaceWhite.copy(alpha = 0.9f),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    PersonAvatar(kind = AvatarKind.SCHOLAR, size = 64.dp, verified = true, badgeColor = Primary)
                }
                Spacer(Modifier.height(12.dp))
                // Status pill — reflects the real latest state (pending vs. reviewed).
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(PrimaryContainer, SurfaceWhite)))
                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val (icon, text) = when {
                        pendingCount > 0 -> Icons.Filled.HourglassTop to
                            "${pendingCount.toBengaliDigits()}টি পড়া ওস্তাদের পর্যালোচনায় আছে…"
                        feedback != null -> Icons.Filled.CheckCircle to
                            "সর্বশেষ ফিডব্যাক: '${feedback.scorePercent.toBengaliDigits()}% সঠিক হয়েছে!'"
                        else -> Icons.Filled.CheckCircle to "এখনও কোনো ফিডব্যাক নেই"
                    }
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    val letter = feedback?.let { state.letterById(it.letterId) }
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DividerGray)
                            .clickable(enabled = letter != null, role = Role.Button) { letter?.let { onSpeak(it.arabicName) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "শুনুন", tint = PrimaryDark, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "পড়া পাঠিয়ে ওস্তাদের অডিও ও উন্নতি করার পরামর্শ (hints) পান",
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfaceWhite,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PillButton(
                        text = "পড়া পাঠান বা ফিডব্যাক দেখুন",
                        onClick = onOpen,
                        containerBrush = GoldBrush,
                        contentColor = PrimaryDark,
                        trailingIcon = Icons.Filled.PlayArrow,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = Secondary, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
private fun AiVoiceCard(onOpen: () -> Unit) {
    GradientCard(
        brush = Brush.linearGradient(listOf(PrimaryDark, Primary)),
        modifier = Modifier.fillMaxWidth(),
        goldRim = false,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("এআই ভয়েস অ্যাসিস্ট্যান্ট", style = MaterialTheme.typography.titleLarge, color = SurfaceWhite)
                Text(
                    "উচ্চারণ ঠিক করুন এআই-এর সাথে",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SurfaceWhite.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(12.dp))
                PillButton(
                    text = "কথা বলুন",
                    onClick = onOpen,
                    containerColor = PrimaryLight,
                    contentColor = PrimaryDark,
                    trailingIcon = Icons.Filled.PlayArrow,
                    height = 42.dp,
                )
            }
            Box(contentAlignment = Alignment.BottomCenter) {
                WaveformBars(
                    modifier = Modifier.size(width = 96.dp, height = 36.dp),
                    animated = true,
                    barCount = 16,
                    colors = WaveformColors,
                )
                AiRobotMascot(size = 84.dp, modifier = Modifier.padding(bottom = 18.dp))
            }
        }
    }
}

@Composable
private fun QuizCard(onOpen: () -> Unit) {
    GradientCard(
        brush = Brush.linearGradient(listOf(PrimaryDark, PrimaryDark)),
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen),
        goldRim = false,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("কুইজ ও পরীক্ষা", style = MaterialTheme.typography.titleLarge, color = SurfaceWhite)
                Text(
                    "কুইজ খেলে নিজেকে যাচাই করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SurfaceWhite.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(12.dp))
                PillButton(
                    text = "কুইজ খেলুন",
                    onClick = onOpen,
                    containerColor = PrimaryLight,
                    contentColor = PrimaryDark,
                    trailingIcon = Icons.Filled.PlayArrow,
                    height = 42.dp,
                )
            }
            TrophyIllustration(size = 88.dp)
        }
    }
}
