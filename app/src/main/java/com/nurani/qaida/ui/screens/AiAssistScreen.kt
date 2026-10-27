package com.nurani.qaida.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.AiEvaluation
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.ui.components.AiRobotMascot
import com.nurani.qaida.ui.components.EmeraldBrush
import com.nurani.qaida.ui.components.GoldBrush
import com.nurani.qaida.ui.components.GradientCard
import com.nurani.qaida.ui.components.InfoStrip
import com.nurani.qaida.ui.components.PillButton
import com.nurani.qaida.ui.components.QaidaCard
import com.nurani.qaida.ui.components.QaidaTopBar
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.SpeakerButton
import com.nurani.qaida.ui.components.WaveformBars
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.ui.viewmodel.RecordingPhase
import com.nurani.qaida.util.formatTimerBn
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryContainer
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.Secondary
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.StripGray
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary
import com.nurani.qaida.ui.theme.TextSecondary
import com.nurani.qaida.ui.theme.WaveformColors

@Composable
fun AiAssistScreen(
    state: QaidaUiState,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onRecord: () -> Unit,
    onStopRecording: () -> Unit,
    onSendToHujur: () -> Unit,
) {
    ScreenBackground {
        val letter = state.selectedLetter
        if (letter == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
            return@ScreenBackground
        }
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            QaidaTopBar(title = "এআই ভয়েস অ্যাসিস্ট্যান্ট", onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MascotGreeting()
                CurrentLetterCard(letter, onSpeak)
                RecorderCard(
                    phase = state.recordingPhase,
                    secondsLeft = state.secondsLeft,
                    onMicClick = if (state.recordingPhase == RecordingPhase.RECORDING) onStopRecording else onRecord,
                )
                EvaluationCard(letter = letter, phase = state.recordingPhase, evaluation = state.aiEvaluation)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillButton(
                        text = "পুনরায় চেষ্টা",
                        onClick = onRecord,
                        containerColor = Primary,
                        leadingIcon = Icons.Filled.Refresh,
                        enabled = !state.isBusyRecording,
                        height = 52.dp,
                        modifier = Modifier.weight(1f),
                    )
                    PillButton(
                        text = if (state.isSubmittingToHujur) "পাঠানো হচ্ছে…" else "ওস্তাদকে পাঠান",
                        onClick = onSendToHujur,
                        containerBrush = GoldBrush,
                        contentColor = PrimaryDark,
                        enabled = !state.isBusyRecording && !state.isSubmittingToHujur,
                        height = 52.dp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MascotGreeting() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AiRobotMascot(size = 112.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            "আসসালামু আলাইকুম!\nআমার সাথে অনুশীলন করুন।",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .shadow(3.dp, SpeechBubbleShape)
                .clip(SpeechBubbleShape)
                .background(SurfaceWhite)
                .border(1.dp, Primary.copy(alpha = 0.4f), SpeechBubbleShape)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}

private val SpeechBubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)

@Composable
private fun CurrentLetterCard(letter: ArabicLetter, onSpeak: (String) -> Unit) {
    QaidaCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                buildAnnotatedString {
                    append("আজকের হরফ: ")
                    withStyle(SpanStyle(color = Primary, fontSize = 34.sp)) { append(letter.letter) }
                    append(" (${letter.nameBn})")
                },
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            SpeakerButton(onClick = { onSpeak(letter.arabicName) }, contentDescription = "${letter.nameBn} শুনুন")
        }
        Text(
            "উচ্চারণ করুন '${letter.nameBn}' (${letter.transliteration})",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().background(StripGray).padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun RecorderCard(phase: RecordingPhase, secondsLeft: Int, onMicClick: () -> Unit) {
    val recording = phase == RecordingPhase.RECORDING
    val evaluating = phase == RecordingPhase.EVALUATING
    val pulse = rememberInfiniteTransition(label = "micPulse")
    val glow by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (recording) 1.12f else 1.03f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )
    GradientCard(brush = EmeraldBrush, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                formatTimerBn(if (recording) secondsLeft else 3),
                color = SurfaceWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                when {
                    recording -> "শুনছি… এখন উচ্চারণ করুন"
                    evaluating -> "এআই মূল্যায়ন করছে…"
                    else -> "প্রস্তুত"
                },
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryLight,
            )
            Spacer(Modifier.height(8.dp))
            WaveformBars(
                modifier = Modifier.fillMaxWidth().height(84.dp),
                animated = recording,
                barCount = 36,
                colors = WaveformColors,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "কথা বলুন",
                    color = SecondaryLight,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .padding(horizontal = 14.dp)
                        .size(84.dp)
                        .scale(glow)
                        .shadow(16.dp, CircleShape, ambientColor = Secondary, spotColor = Secondary)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(SecondaryLight, Secondary, SecondaryDark)))
                        .border(3.dp, Color(0x55FFFFFF), CircleShape)
                        .clickable(enabled = !evaluating, role = Role.Button, onClick = onMicClick)
                        .semantics {
                            contentDescription = if (recording) "রেকর্ডিং থামান" else "মাইক চেপে উচ্চারণ করুন"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (evaluating) {
                        CircularProgressIndicator(color = PrimaryDark, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
                    } else {
                        Icon(
                            if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = null,
                            tint = PrimaryDark,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
                Text(
                    "মাইক চাপুন",
                    color = SecondaryLight,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun EvaluationCard(letter: ArabicLetter, phase: RecordingPhase, evaluation: AiEvaluation?) {
    QaidaCard(Modifier.fillMaxWidth(), borderColor = Primary.copy(alpha = 0.5f)) {
        AnimatedContent(
            targetState = Triple(phase == RecordingPhase.EVALUATING, evaluation, letter.id),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "evaluation",
        ) { (evaluating, eval, _) ->
            when {
                evaluating -> Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("আপনার উচ্চারণ যাচাই করা হচ্ছে…", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                }
                eval != null -> EvaluationResult(eval)
                else -> Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("এআই মূল্যায়ন", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    InfoStrip("সোনালি মাইক চেপে '${letter.nameBn}' উচ্চারণ করুন। এআই সাথে সাথে আপনার উচ্চারণ যাচাই করবে।")
                }
            }
        }
    }
}

@Composable
private fun EvaluationResult(eval: AiEvaluation) {
    val scoreColor = when {
        eval.scorePercent >= 90 -> Primary
        eval.scorePercent >= 80 -> Primary
        else -> SecondaryDark
    }
    Column {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CheckRing(color = scoreColor)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("এআই মূল্যায়ন", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    "${eval.scorePercent.toBengaliDigits()}% সঠিক হয়েছে!",
                    color = scoreColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = scoreColor, modifier = Modifier.size(28.dp).align(Alignment.Top))
        }
        Column(
            Modifier.fillMaxWidth().background(PrimaryContainer).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(eval.messageBn, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, textAlign = TextAlign.Center)
            eval.tipsBn.forEach { tip ->
                Text("• $tip", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

/** Large ring with a check mark, drawn so it can take the score colour. */
@Composable
private fun CheckRing(color: Color) {
    Canvas(Modifier.size(64.dp)) {
        val stroke = 6.dp.toPx()
        drawArc(
            color.copy(alpha = 0.2f), 0f, 360f, false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
            style = Stroke(stroke),
        )
        drawArc(
            color, -90f, 300f, false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        val check = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.52f)
            lineTo(size.width * 0.45f, size.height * 0.66f)
            lineTo(size.width * 0.72f, size.height * 0.38f)
        }
        drawPath(check, color, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}
