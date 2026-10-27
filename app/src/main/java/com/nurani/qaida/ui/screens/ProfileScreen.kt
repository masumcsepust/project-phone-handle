package com.nurani.qaida.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.data.model.RecentActivity
import com.nurani.qaida.data.model.UserProfile
import com.nurani.qaida.ui.components.AchievementBadge
import com.nurani.qaida.ui.components.AudioPlayerRow
import com.nurani.qaida.ui.components.AvatarKind
import com.nurani.qaida.ui.components.BadgePill
import com.nurani.qaida.ui.components.PersonAvatar
import com.nurani.qaida.ui.components.QaidaCard
import com.nurani.qaida.ui.components.QaidaProgressBar
import com.nurani.qaida.ui.components.QaidaTopBar
import com.nurani.qaida.ui.components.ScreenBackground
import com.nurani.qaida.ui.components.SectionTitle
import com.nurani.qaida.ui.viewmodel.QaidaUiState
import com.nurani.qaida.util.formatTimerBn
import com.nurani.qaida.util.toBengaliDigits
import com.nurani.qaida.ui.theme.DividerGray
import com.nurani.qaida.ui.theme.Primary
import com.nurani.qaida.ui.theme.PrimaryContainer
import com.nurani.qaida.ui.theme.PrimaryLight
import com.nurani.qaida.ui.theme.PrimaryDark
import com.nurani.qaida.ui.theme.Secondary
import com.nurani.qaida.ui.theme.SecondaryDark
import com.nurani.qaida.ui.theme.SecondaryLight
import com.nurani.qaida.ui.theme.StripGray
import com.nurani.qaida.ui.theme.SurfaceWhite
import com.nurani.qaida.ui.theme.TextPrimary
import com.nurani.qaida.ui.theme.TextSecondary

private val BadgePalettes = listOf(
    listOf(PrimaryLight, PrimaryDark),
    listOf(Secondary, SecondaryDark),
)

@Composable
fun ProfileScreen(
    state: QaidaUiState,
    onSpeak: (String) -> Unit,
    onMenuClick: (String) -> Unit,
) {
    ScreenBackground {
        val profile = state.profile
        if (profile == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
            return@ScreenBackground
        }
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            QaidaTopBar(title = "আমার প্রোফাইল", centerTitle = true) {
                IconButton(onClick = { onMenuClick("সেটিংস") }) {
                    Icon(Icons.Outlined.Settings, contentDescription = "সেটিংস", tint = PrimaryDark)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UserHeader(profile)
                SectionTitle("আমার অর্জন")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                ) {
                    itemsIndexed(profile.achievements, key = { _, a -> a.id }) { i, a ->
                        AchievementBadge(
                            title = a.titleBn,
                            colors = BadgePalettes[i % BadgePalettes.size],
                            unlocked = a.unlocked,
                            size = 100.dp,
                        )
                    }
                }
                SectionTitle("পরিসংখ্যান")
                StatsGrid(profile)
                if (profile.recentActivity.isNotEmpty()) {
                    SectionTitle("সাম্প্রতিক কার্যক্রম")
                    profile.recentActivity.take(3).forEachIndexed { i, activity ->
                        RecentActivityCard(activity, state, onSpeak, initiallyExpanded = i == 0)
                    }
                }
                MenuList(onMenuClick)
            }
        }
    }
}

@Composable
private fun UserHeader(profile: UserProfile) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PersonAvatar(kind = AvatarKind.BOY, size = 112.dp)
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(profile.nameBn, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp), color = PrimaryDark)
            BadgePill(
                text = "স্তর: ${profile.level.toBengaliDigits()} (লেভেল ${profile.level.toBengaliDigits()})",
                containerColor = SecondaryLight,
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceWhite)
                    .padding(4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                QaidaProgressBar(progress = profile.qaidaCompletionPercent / 100f, height = 18.dp)
                Text(
                    "${profile.qaidaCompletionPercent.toBengaliDigits()}% কায়দা সমাপ্ত",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(profile: UserProfile) {
    val stats = listOf(
        "${profile.lettersLearned.toBengaliDigits()}/${profile.totalLetters.toBengaliDigits()}" to "শেখা হরফ",
        "${profile.aiPracticeCount.toBengaliDigits()} বার" to "এআই অনুশীলন",
        "${profile.ustadReviewCount.toBengaliDigits()}টি" to "ওস্তাদের যাচাই",
        profile.quizPoints.toBengaliDigits() to "কুইজ পয়েন্ট",
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (value, label) ->
                    QaidaCard(Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            value,
                            color = Primary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 4.dp),
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StripGray)
                                .padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    activity: RecentActivity,
    state: QaidaUiState,
    onSpeak: (String) -> Unit,
    initiallyExpanded: Boolean,
) {
    val letter = state.letterById(activity.letterId) ?: return
    var expanded by rememberSaveable(activity) { mutableStateOf(initiallyExpanded) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
    QaidaCard(Modifier.fillMaxWidth(), onClick = { expanded = !expanded }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(letter.letter, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("হরফ: ${letter.letter} (${letter.nameBn})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(
                    "${activity.scorePercent.toBengaliDigits()}% সঠিক" +
                        if (activity.verifiedByUstad) " - ওস্তাদ ভেরিফাইড" else " - এআই মূল্যায়ন",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                )
            }
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "সংকুচিত করুন" else "বিস্তারিত দেখুন",
                tint = TextSecondary,
                modifier = Modifier.rotate(arrowRotation),
            )
        }
        AnimatedVisibility(expanded) {
            Row(
                Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AudioPlayerRow(
                    durationSeconds = activity.audioSeconds,
                    onPlay = { onSpeak(letter.arabicName) },
                    playButtonFirst = true,
                    waveColor = SecondaryLight,
                    inactiveWaveColor = Color(0x66FFFFFF),
                    buttonColor = PrimaryDark,
                    trailingLabel = formatTimerBn(activity.audioSeconds),
                    labelColor = SurfaceWhite,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(Primary)
                        .padding(4.dp)
                        .padding(end = 10.dp),
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable(role = Role.Button) { onSpeak(letter.arabicName) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "সঠিক উচ্চারণ শুনুন", tint = SurfaceWhite)
                }
            }
        }
    }
}

@Composable
private fun MenuList(onMenuClick: (String) -> Unit) {
    val items = listOf(
        Icons.Outlined.Group to "আমার ওস্তাদগণ",
        Icons.Outlined.CheckBox to "দৈনিক লক্ষ্য ও প্রোগ্রেস",
        Icons.Outlined.Settings to "সেটিংস ও নোটিফিকেশন",
    )
    Column {
        items.forEachIndexed { i, (icon, label) ->
            MenuRow(icon, label) { onMenuClick(label) }
            if (i < items.lastIndex) HorizontalDivider(color = DividerGray)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary)
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
    }
}
