package com.nurani.qaida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.HujurFeedback
import com.nurani.qaida.data.model.HujurSubmission
import com.nurani.qaida.ui.components.AudioPlayerRow
import com.nurani.qaida.ui.components.AvatarKind
import com.nurani.qaida.ui.components.EmeraldBrush
import com.nurani.qaida.ui.components.GradientCard
import com.nurani.qaida.ui.components.InfoStrip
import com.nurani.qaida.ui.components.IslamicHeaderBanner
import com.nurani.qaida.ui.components.PersonAvatar
import com.nurani.qaida.ui.components.PillButton
import com.nurani.qaida.ui.components.QaidaCard
import com.nurani.qaida.ui.components.ScoreGauge
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.SectionTitle
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryContainer
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.Secondary
import com.nurani.qaida.ui.theme.SecondaryContainer
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary
import com.nurani.qaida.ui.theme.TextSecondary

@Composable
fun HujurVerifyScreen(
    state: QaidaUiState,
    onSpeak: (String) -> Unit,
    onRecordAgain: () -> Unit,
    onNextLesson: () -> Unit,
    onUstadAudioPlayed: () -> Unit,
) {
    ScreenBackground {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IslamicHeaderBanner(
                    greeting = "আসসালামু আলাইকুম!",
                    ribbonText = "ওস্তাদ বা হুজুর যাচাই (Hujur Feedback)",
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    PersonAvatar(kind = AvatarKind.USER, size = 46.dp)
                }

                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.pendingSubmissions.isNotEmpty()) {
                        PendingCard(state.pendingSubmissions, state)
                    }
                    SectionTitle("আপনার সর্বশেষ পরীক্ষা", modifier = Modifier.padding(top = 4.dp))
                    val feedback = state.hujurFeedback
                    if (feedback == null) {
                        InfoStrip(
                            "এখনও কোনো ফিডব্যাক নেই। এআই স্ক্রিনে রেকর্ড করে ওস্তাদকে পাঠান — উনি শুনে নম্বর ও পরামর্শ দেবেন।",
                            color = SurfaceWhite,
                        )
                    } else {
                        val letter = state.letterById(feedback.letterId)
                        ScholarScoreCard(feedback, letterLabel = letter?.let { "হরফ: ${it.letter} (${it.nameBn})" })
                        QaidaCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("আপনার পাঠানো ভয়েস", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Spacer(Modifier.height(6.dp))
                                AudioPlayerRow(
                                    durationSeconds = feedback.studentAudioSeconds,
                                    onPlay = { letter?.let { onSpeak(it.arabicName) } },
                                    seed = 11,
                                )
                            }
                        }
                        QaidaCard(Modifier.fillMaxWidth(), containerColor = PrimaryContainer) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "ওস্তাদের অডিও ফিডব্যাক",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (!feedback.isUstadAudioHeard) {
                                        Text(
                                            "নতুন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SurfaceWhite,
                                            modifier = Modifier
                                                .background(SecondaryDark, RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                AudioPlayerRow(
                                    durationSeconds = feedback.ustadAudioSeconds,
                                    onPlay = {
                                        letter?.let { onSpeak(it.arabicName) }
                                        onUstadAudioPlayed()
                                    },
                                    trailingLabel = if (feedback.isUstadAudioHeard) "Heard" else null,
                                    waveColor = TextSecondary,
                                    seed = 23,
                                )
                            }
                        }
                        FeedbackSection(feedback.improvementsBn)
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PillButton(
                    text = "আবার রেকর্ড করুন",
                    onClick = onRecordAgain,
                    containerColor = SecondaryDark,
                    leadingIcon = Icons.Filled.Mic,
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    text = "পরবর্তী পাঠে যান",
                    onClick = onNextLesson,
                    containerColor = PrimaryLight,
                    contentColor = PrimaryDark,
                    trailingIcon = Icons.Filled.PlayArrow,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PendingCard(pending: List<HujurSubmission>, state: QaidaUiState) {
    QaidaCard(Modifier.fillMaxWidth(), containerColor = SecondaryContainer, borderColor = SecondaryLight) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("পর্যালোচনার অপেক্ষায়", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            pending.forEach { sub ->
                val letter = state.letterById(sub.letterId)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Secondary, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${letter?.letter ?: ""} (${letter?.nameBn ?: ""}) — ওস্তাদ শীঘ্রই শুনে ফিডব্যাক দেবেন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScholarScoreCard(feedback: HujurFeedback, letterLabel: String?) {
    GradientCard(brush = EmeraldBrush, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                PersonAvatar(
                    kind = AvatarKind.SCHOLAR,
                    size = 104.dp,
                    verified = feedback.ustad.isVerified,
                    ring = Color.White.copy(alpha = 0.25f),
                )
                Spacer(Modifier.height(6.dp))
                Text(feedback.ustad.nameBn, color = SurfaceWhite, style = MaterialTheme.typography.titleMedium)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${feedback.scorePercent.toBengaliDigits()}% সঠিক হয়েছে!",
                    color = Secondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                ScoreGauge(percent = feedback.scorePercent, size = 108.dp)
                if (letterLabel != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(letterLabel, color = SecondaryLight, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FeedbackSection(improvements: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle("ফিডব্যাক ও সংশোধন")
        WarningLine("কী কী উন্নতি করতে হবে?", bold = true)
        if (improvements.isEmpty()) {
            Text("মাশাআল্লাহ! কোনো সংশোধন নেই।", style = MaterialTheme.typography.bodyMedium, color = Primary)
        }
        improvements.forEachIndexed { i, text ->
            WarningLine("${(i + 1).toBengaliDigits()}. $text")
        }
    }
}

@Composable
private fun WarningLine(text: String, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
    }
}
