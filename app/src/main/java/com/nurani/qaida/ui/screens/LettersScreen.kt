package com.nurani.qaida.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.ArabicLetter
import com.nurani.qaida.data.model.ExampleWord
import com.nurani.qaida.data.model.LetterPosition
import com.nurani.qaida.data.model.PositionalForm
import com.nurani.qaida.ui.components.AiRobotMascot
import com.nurani.qaida.ui.components.BadgePill
import com.nurani.qaida.ui.components.EmeraldBrush
import com.nurani.qaida.ui.components.GoldBrush
import com.nurani.qaida.ui.components.GradientCard
import com.nurani.qaida.ui.components.PillButton
import com.nurani.qaida.ui.components.QaidaCard
import com.nurani.qaida.ui.components.QaidaTopBar
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.SectionTitle
import com.nurani.qaida.ui.components.SpeakerButton
import com.nurani.qaida.ui.components.WaveformBars
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.CardBorder
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.Secondary
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.StripGray
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary
import com.nurani.qaida.ui.theme.TextSecondary
import com.nurani.qaida.ui.theme.WaveformColors

@Composable
fun LettersScreen(
    state: QaidaUiState,
    onBack: () -> Unit,
    onSelectLetter: (Int) -> Unit,
    onNextLetter: () -> Unit,
    onSpeak: (String) -> Unit,
    onPracticeWithAi: () -> Unit,
    onSendToHujur: () -> Unit,
) {
    ScreenBackground {
        val letter = state.selectedLetter
        if (letter == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
            return@ScreenBackground
        }
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            QaidaTopBar(title = "আরবি হরফ ও অবস্থান", onBack = onBack) {
                BadgePill(
                    text = "${(state.selectedIndex + 1).toBengaliDigits()}/${state.letters.size.toBengaliDigits()} হরফ",
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            LetterCarousel(
                letters = state.letters,
                selectedIndex = state.selectedIndex,
                onSelect = onSelectLetter,
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedContent(
                    targetState = letter,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "letter",
                ) { current ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LetterShowcase(current, onSpeak)
                        SectionTitle("হরফের অবস্থান ও রূপ", center = true)
                        PositionGrid(current, onSpeak)
                    }
                }
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("উচ্চারণ প্র্যাকটিস করুন ") }
                        append("(Practice Pronunciation)")
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal),
                    color = TextPrimary,
                )
                PracticeBanner(isRecording = state.isBusyRecording, onMicClick = onPracticeWithAi)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillButton(
                        text = if (state.isSubmittingToHujur) "পাঠানো হচ্ছে…" else "ওস্তাদকে যাচাই করতে পাঠান",
                        onClick = onSendToHujur,
                        containerBrush = GoldBrush,
                        contentColor = PrimaryDark,
                        trailingIcon = Icons.Filled.Mic,
                        enabled = !state.isSubmittingToHujur,
                        height = 54.dp,
                        modifier = Modifier.weight(1f),
                    )
                    PillButton(
                        text = "পরবর্তী হরফ",
                        onClick = onNextLetter,
                        containerColor = Primary,
                        trailingIcon = Icons.Filled.PlayArrow,
                        height = 54.dp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterCarousel(letters: List<ArabicLetter>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(letters, key = { _, l -> l.id }) { index, l ->
            val selected = index == selectedIndex
            val scale by animateFloatAsState(if (selected) 1.06f else 1f, label = "chipScale")
            val shape = RoundedCornerShape(12.dp)
            Column(
                Modifier
                    .scale(scale)
                    .size(width = 56.dp, height = 70.dp)
                    .shadow(if (selected) 6.dp else 0.dp, shape)
                    .clip(shape)
                    .background(if (selected) PrimaryLight else SurfaceWhite)
                    .border(1.dp, if (selected) Primary else CardBorder, shape)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .semantics { contentDescription = "হরফ ${l.nameBn}" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    l.letter,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) PrimaryDark else TextPrimary,
                )
                Text(
                    l.nameBn,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) PrimaryDark else TextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun LetterShowcase(letter: ArabicLetter, onSpeak: (String) -> Unit) {
    QaidaCard(Modifier.fillMaxWidth(), borderColor = Primary.copy(alpha = 0.45f)) {
        Box(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { onSpeak(letter.arabicName) }
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(letter.letter, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text(
                    "${letter.nameBn} (${letter.transliteration})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                )
            }
            SpeakerButton(
                onClick = { onSpeak(letter.arabicName) },
                size = 52.dp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                contentDescription = "${letter.nameBn} শুনুন",
            )
        }
    }
}

@Composable
private fun PositionGrid(letter: ArabicLetter, onSpeak: (String) -> Unit) {
    val rows = listOf(
        LetterPosition.ISOLATED to LetterPosition.INITIAL,
        LetterPosition.MEDIAL to LetterPosition.FINAL,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (a, b) ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PositionCard(letter, letter.form(a), onSpeak, Modifier.weight(1f))
                PositionCard(letter, letter.form(b), onSpeak, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PositionCard(
    letter: ArabicLetter,
    form: PositionalForm,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val position = form.position
    val formColor = if (position == LetterPosition.INITIAL) SecondaryDark else Primary
    QaidaCard(modifier, shape = RoundedCornerShape(12.dp)) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(position.titleBn) }
                append(" (${position.titleEn})")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                form.form,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = formColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { onSpeak(letter.arabicName) }
                    .padding(horizontal = 4.dp),
            )
            when {
                position == LetterPosition.ISOLATED -> SpeakerButton(
                    onClick = { onSpeak(letter.arabicName) },
                    size = 38.dp,
                    contentDescription = "${letter.nameBn} শুনুন",
                )
                form.example != null -> ExampleWordText(form.example, onSpeak)
            }
        }
        Text(
            text = form.example?.meaningBn?.let { "${position.stripBn} · $it" } ?: position.stripBn,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(StripGray)
                .padding(vertical = 6.dp, horizontal = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** Arabic example word where ONLY the target letter is drawn in red. */
@Composable
private fun ExampleWordText(word: ExampleWord, onSpeak: (String) -> Unit) {
    Text(
        text = buildAnnotatedString {
            append(word.before)
            withStyle(SpanStyle(color = SecondaryDark)) { append(word.target) }
            append(word.after)
        },
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button) { onSpeak(word.full) }
            .padding(horizontal = 4.dp)
            .semantics { contentDescription = "উদাহরণ শব্দ, ${word.meaningBn}" },
    )
}

@Composable
private fun PracticeBanner(isRecording: Boolean, onMicClick: () -> Unit) {
    GradientCard(
        brush = EmeraldBrush,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AiRobotMascot(size = 68.dp)
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(
                    "এআই এর সাথে উচ্চারণ প্র্যাকটিস করুন",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SurfaceWhite,
                )
                Spacer(Modifier.height(8.dp))
                WaveformBars(
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    animated = isRecording,
                    barCount = 26,
                    colors = WaveformColors,
                )
            }
            Column(
                Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape, ambientColor = Secondary, spotColor = Secondary)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(SecondaryLight, Secondary)))
                    .clickable(role = Role.Button, onClick = onMicClick)
                    .semantics { contentDescription = "মাইক চেপে উচ্চারণ করুন" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(26.dp))
                Text("বলুন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryDark)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}
