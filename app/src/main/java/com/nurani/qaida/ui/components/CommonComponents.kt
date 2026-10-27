package com.nurani.qaida.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurani.qaida.util.toBengaliDigits
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import com.nurani.qaida.ui.theme.CardBorder
import com.nurani.qaida.ui.theme.DividerGray
import com.nurani.qaida.ui.theme.OutlineGray
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

// ---------------------------------------------------------------------------
// Brushes & layout scaffolding
// ---------------------------------------------------------------------------

val EmeraldBrush = Brush.linearGradient(listOf(PrimaryDark, Primary))
val TealBrush = Brush.linearGradient(listOf(PrimaryDark, Primary))
val GoldBrush = Brush.horizontalGradient(listOf(SecondaryLight, Secondary))
val ScreenBackgroundBrush = Brush.verticalGradient(
    0f to Color(0xFFD5F5E3),
    0.18f to PrimaryContainer,
    0.38f to PrimaryContainer,
    1f to Color(0xFFF7FBF9),
)

/** Full-screen mint → sky gradient used behind every tab. */
@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackgroundBrush),
        content = content,
    )
}

@Composable
fun QaidaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    centerTitle: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "পিছনে যান",
                    tint = PrimaryDark,
                )
            }
        } else {
            Spacer(Modifier.width(if (centerTitle) 48.dp else 12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryDark,
            textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        if (centerTitle && onBack == null) Spacer(Modifier.width(4.dp))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, center: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------------------
// Cards & buttons
// ---------------------------------------------------------------------------

/** White rounded card with a thin mint border — the app's base surface. */
@Composable
fun QaidaCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = SurfaceWhite,
    borderColor: Color? = CardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .shadow(elevation = 3.dp, shape = shape, ambientColor = PrimaryDark, spotColor = PrimaryDark)
        .clip(shape)
        .background(containerColor)
        .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Column(modifier = base, content = content)
}

/** Gradient hero card with optional gold rim and faint Islamic pattern. */
@Composable
fun GradientCard(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    goldRim: Boolean = true,
    pattern: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(8.dp, shape, ambientColor = PrimaryDark, spotColor = PrimaryDark)
            .clip(shape)
            .background(brush)
            .then(
                if (goldRim) Modifier.border(
                    BorderStroke(2.dp, Brush.linearGradient(listOf(SecondaryLight, Secondary, SecondaryDark))),
                    shape,
                ) else Modifier
            ),
    ) {
        if (pattern) IslamicPatternOverlay(Modifier.matchParentSize(), color = Color.White, alpha = 0.06f)
        Box(Modifier.padding(contentPadding), content = content)
    }
}

/** Rounded "pill" call-to-action used across all screens. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Primary,
    containerBrush: Brush? = null,
    contentColor: Color = SurfaceWhite,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(height)
            .shadow(if (enabled) 4.dp else 0.dp, shape)
            .clip(shape)
            .then(
                if (containerBrush != null) Modifier.background(containerBrush)
                else Modifier.background(containerColor)
            )
            .alpha(if (enabled) 1f else 0.55f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(6.dp))
            Icon(trailingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
    }
}

/** Circular speaker button that plays TTS for a letter/word. */
@Composable
fun SpeakerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = Primary,
    iconTint: Color = SurfaceWhite,
    contentDescription: String = "শুনুন",
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
fun BadgePill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SecondaryLight,
    contentColor: Color = PrimaryDark,
) {
    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Two-tone rounded progress bar (filled part + lighter "almost there" glow). */
@Composable
fun QaidaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = DividerGray,
    fillBrush: Brush = Brush.horizontalGradient(listOf(Primary, Primary)),
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "progress",
    )
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(trackColor, cornerRadius = r)
        val glowW = (size.width * (animated + 0.12f)).coerceAtMost(size.width)
        drawRoundRect(PrimaryLight.copy(alpha = 0.35f), size = Size(glowW, size.height), cornerRadius = r)
        drawRoundRect(fillBrush, size = Size(size.width * animated, size.height), cornerRadius = r)
    }
}

// ---------------------------------------------------------------------------
// Islamic decoration
// ---------------------------------------------------------------------------

/** Tiling of 8-pointed stars (Rub el Hizb style) drawn as thin outlines. */
@Composable
fun IslamicPatternOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.08f,
    cell: Dp = 44.dp,
) {
    Canvas(modifier) {
        val step = cell.toPx()
        val r = step * 0.36f
        val stroke = Stroke(width = 1.dp.toPx())
        val c = color.copy(alpha = alpha)
        var y = 0f
        var row = 0
        while (y < size.height + step) {
            var x = if (row % 2 == 0) 0f else step / 2
            while (x < size.width + step) {
                drawEightPointStar(Offset(x, y), r, c, stroke)
                x += step
            }
            y += step * 0.75f
            row++
        }
    }
}

private fun DrawScope.drawEightPointStar(center: Offset, radius: Float, color: Color, stroke: Stroke) {
    for (offsetDeg in listOf(0.0, 45.0)) {
        val path = Path()
        for (i in 0 until 4) {
            val a = (offsetDeg + i * 90.0) * PI / 180.0
            val p = Offset(center.x + (radius * cos(a)).toFloat(), center.y + (radius * sin(a)).toFloat())
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        path.close()
        drawPath(path, color, style = stroke)
    }
}

/** Faint mosque silhouette with crescent, used behind the home greeting. */
@Composable
fun MosqueSilhouette(modifier: Modifier = Modifier, color: Color = Primary, alpha: Float = 0.14f) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = color.copy(alpha = alpha)
        // Base hall
        drawRect(c, topLeft = Offset(w * 0.22f, h * 0.62f), size = Size(w * 0.56f, h * 0.38f))
        // Main dome
        drawArc(c, 180f, 180f, true, topLeft = Offset(w * 0.28f, h * 0.30f), size = Size(w * 0.44f, h * 0.64f))
        // Minarets
        for (fx in listOf(0.08f, 0.84f)) {
            drawRect(c, topLeft = Offset(w * fx, h * 0.25f), size = Size(w * 0.08f, h * 0.75f))
            drawArc(c, 180f, 180f, true, topLeft = Offset(w * fx, h * 0.16f), size = Size(w * 0.08f, h * 0.18f))
        }
        // Crescent
        val cc = Offset(w * 0.5f, h * 0.14f)
        val cr = h * 0.09f
        drawCircle(c, radius = cr, center = cc)
        drawCircle(Color.White.copy(alpha = 0.9f), radius = cr * 0.85f, center = cc + Offset(cr * 0.45f, -cr * 0.15f))
    }
}

/**
 * Dark-emerald banner with geometric pattern, greeting and a gold ribbon —
 * the header of the Hujur screen.
 */
@Composable
fun IslamicHeaderBanner(
    greeting: String,
    ribbonText: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
        ) {
            IslamicPatternOverlay(Modifier.matchParentSize(), alpha = 0.12f, cell = 36.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 34.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SurfaceWhite,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
                Spacer(Modifier.width(10.dp))
                trailing()
            }
        }
        Text(
            text = ribbonText,
            color = SurfaceWhite,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 16.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryDark)
                .border(2.dp, GoldBrush, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 7.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Characters: AI robot & people avatars (vector-drawn so they scale crisply)
// ---------------------------------------------------------------------------

/** Friendly AI robot wearing a white Islamic tupi (prayer cap). */
@Composable
fun AiRobotMascot(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val transition = rememberInfiniteTransition(label = "robot")
    val bob by transition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    Canvas(
        modifier
            .size(size)
            .offset(y = bob.dp)
            .semantics { contentDescription = "এআই রোবট সহকারী" }
    ) {
        val u = this.size.minDimension / 100f
        fun o(x: Float, y: Float) = Offset(x * u, y * u)
        fun s(w: Float, h: Float) = Size(w * u, h * u)
        val outline = OutlineGray

        // Body + emblem
        drawRoundRect(SurfaceWhite, o(30f, 70f), s(40f, 28f), CornerRadius(12f * u))
        drawRoundRect(outline, o(30f, 70f), s(40f, 28f), CornerRadius(12f * u), style = Stroke(1.2f * u))
        drawCircle(Primary, 5.5f * u, o(50f, 84f))
        drawCircle(Secondary, 2.2f * u, o(50f, 84f))
        // Arm (waving)
        drawRoundRect(SurfaceWhite, o(70f, 66f), s(10f, 20f), CornerRadius(5f * u))
        drawRoundRect(outline, o(70f, 66f), s(10f, 20f), CornerRadius(5f * u), style = Stroke(1.2f * u))
        drawRoundRect(SurfaceWhite, o(21f, 74f), s(10f, 18f), CornerRadius(5f * u))
        // Headphones
        for (x in listOf(15f, 85f)) {
            drawCircle(Secondary, 9f * u, o(x, 50f))
            drawCircle(Primary, 5f * u, o(x, 50f))
        }
        // Head
        drawRoundRect(SurfaceWhite, o(18f, 26f), s(64f, 46f), CornerRadius(22f * u))
        drawRoundRect(outline, o(18f, 26f), s(64f, 46f), CornerRadius(22f * u), style = Stroke(1.2f * u))
        // Visor
        drawRoundRect(PrimaryDark, o(26f, 36f), s(48f, 28f), CornerRadius(14f * u))
        // Eyes (happy arcs) + smile
        val eyeStroke = Stroke(width = 3.2f * u, cap = StrokeCap.Round)
        drawArc(PrimaryLight, 200f, 140f, false, o(35f, 44f), s(10f, 10f), style = eyeStroke)
        drawArc(PrimaryLight, 200f, 140f, false, o(55f, 44f), s(10f, 10f), style = eyeStroke)
        drawArc(PrimaryLight, 20f, 140f, false, o(42f, 50f), s(16f, 9f), style = Stroke(2.6f * u, cap = StrokeCap.Round))
        // Tupi (cap): dome + embroidered band
        drawArc(SurfaceWhite, 180f, 180f, true, o(24f, 6f), s(52f, 36f))
        drawArc(outline, 180f, 180f, false, o(24f, 6f), s(52f, 36f), style = Stroke(1.2f * u))
        drawRoundRect(SurfaceWhite, o(22f, 21f), s(56f, 10f), CornerRadius(4f * u))
        drawRoundRect(outline, o(22f, 21f), s(56f, 10f), CornerRadius(4f * u), style = Stroke(1f * u))
        var dx = 27f
        while (dx < 76f) {
            drawCircle(if ((dx.toInt() / 6) % 2 == 0) Secondary else Primary, 1.3f * u, o(dx, 26f))
            dx += 6f
        }
        drawCircle(Color(0xFFE2E8F0), 1.6f * u, o(50f, 13f))
    }
}

enum class AvatarKind { SCHOLAR, USER, BOY }

/** Illustrated avatar of a person wearing a white tupi, with optional verified badge. */
@Composable
fun PersonAvatar(
    kind: AvatarKind,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    verified: Boolean = false,
    ring: Color? = SurfaceWhite,
    badgeColor: Color = PrimaryLight,
) {
    val description = when (kind) {
        AvatarKind.SCHOLAR -> "ওস্তাদের ছবি"
        AvatarKind.USER, AvatarKind.BOY -> "প্রোফাইল ছবি"
    }
    Box(modifier.size(size).semantics { contentDescription = description }) {
        Canvas(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        if (kind == AvatarKind.BOY) listOf(Color(0xFFFFF7E6), Color(0xFFFDE7C2))
                        else listOf(Color(0xFFD1FAE5), Color(0xFF99D8C4))
                    )
                )
        ) { drawPerson(kind) }
        if (ring != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .border(
                        width = (size.value / 28f).coerceAtLeast(2f).dp,
                        brush = if (kind == AvatarKind.BOY) GoldBrush else Brush.linearGradient(listOf(ring, ring)),
                        shape = CircleShape,
                    )
            )
        }
        if (verified) {
            val badge = size * 0.34f
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(badge)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .padding(badge * 0.1f)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "ভেরিফাইড",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(badge * 0.62f),
                )
            }
        }
    }
}

private fun DrawScope.drawPerson(kind: AvatarKind) {
    val u = size.minDimension / 100f
    fun o(x: Float, y: Float) = Offset(x * u, y * u)
    fun s(w: Float, h: Float) = Size(w * u, h * u)
    val skin = if (kind == AvatarKind.BOY) Color(0xFFF5C9A0) else Color(0xFFE8B48A)
    val beard = Color(0xFF3B2A20)
    val cloth = when (kind) {
        AvatarKind.SCHOLAR -> Color(0xFFE9DFC9)
        AvatarKind.USER -> PrimaryDark
        AvatarKind.BOY -> Primary
    }

    // Shoulders & neck
    drawOval(cloth, o(14f, 76f), s(72f, 50f))
    drawRect(skin, o(43f, 64f), s(14f, 14f))
    if (kind != AvatarKind.BOY) drawOval(beard, o(28f, 44f), s(44f, 46f))

    // Face
    val faceTop = if (kind == AvatarKind.BOY) 28f else 26f
    drawOval(skin, o(32f, faceTop), s(36f, if (kind == AvatarKind.BOY) 44f else 40f))
    // Ears
    drawOval(skin, o(28f, 44f), s(7f, 11f))
    drawOval(skin, o(65f, 44f), s(7f, 11f))

    // Eyes & brows
    val eye = Color(0xFF1F2937)
    drawCircle(eye, 2.4f * u, o(43f, 47f))
    drawCircle(eye, 2.4f * u, o(57f, 47f))
    val brow = Stroke(width = 1.8f * u, cap = StrokeCap.Round)
    drawLine(beard, o(39f, 41.5f), o(46f, 41f), strokeWidth = brow.width, cap = StrokeCap.Round)
    drawLine(beard, o(54f, 41f), o(61f, 41.5f), strokeWidth = brow.width, cap = StrokeCap.Round)

    if (kind == AvatarKind.BOY) {
        drawCircle(Color(0xFFF59E9E).copy(alpha = 0.45f), 3.5f * u, o(39f, 56f))
        drawCircle(Color(0xFFF59E9E).copy(alpha = 0.45f), 3.5f * u, o(61f, 56f))
        drawArc(Color(0xFF9A3412), 20f, 140f, false, o(43f, 54f), s(14f, 9f), style = Stroke(2f * u, cap = StrokeCap.Round))
    } else {
        // Mustache + lower beard overlapping face
        val beardPath = Path().apply {
            moveTo(32f * u, 52f * u)
            quadraticTo(34f * u, 66f * u, 50f * u, 66f * u)
            quadraticTo(66f * u, 66f * u, 68f * u, 52f * u)
            lineTo(70f * u, 58f * u)
            quadraticTo(68f * u, 90f * u, 50f * u, 92f * u)
            quadraticTo(32f * u, 90f * u, 30f * u, 58f * u)
            close()
        }
        drawPath(beardPath, beard)
        drawArc(beard, 180f, 180f, true, o(41f, 55f), s(18f, 8f))
        drawArc(Color(0xFFB45353), 20f, 140f, false, o(45f, 58f), s(10f, 6f), style = Stroke(1.6f * u, cap = StrokeCap.Round))
    }

    // White tupi
    val capOutline = OutlineGray
    drawArc(SurfaceWhite, 180f, 180f, true, o(31f, 14f), s(38f, 28f))
    drawRoundRect(SurfaceWhite, o(30f, 26f), s(40f, 8f), CornerRadius(3f * u))
    drawRoundRect(capOutline, o(30f, 26f), s(40f, 8f), CornerRadius(3f * u), style = Stroke(0.8f * u))
    var dx = 34f
    while (dx < 68f) {
        drawCircle(capOutline, 0.9f * u, o(dx, 30f))
        dx += 5f
    }
}

// ---------------------------------------------------------------------------
// Audio visuals
// ---------------------------------------------------------------------------

/** Pseudo-random but stable bar heights so waveforms look natural. */
private fun waveformHeights(count: Int, seed: Int): List<Float> = List(count) { i ->
    val center = 1f - abs(i - count / 2f) / (count / 2f) // taller in the middle
    val noise = abs(sin((i + 1) * 12.9898f + seed * 78.233f) * 43758.547f) % 1f
    (0.18f + 0.55f * center + 0.35f * noise).coerceIn(0.12f, 1f)
}

/**
 * Vertical waveform bars. When [animated] the bars pulse with
 * [rememberInfiniteTransition]; [progress] (0..1) tints the played part.
 */
@Composable
fun WaveformBars(
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    barCount: Int = 32,
    colors: List<Color> = listOf(PrimaryDark),
    inactiveColor: Color? = null,
    progress: Float = 1f,
    seed: Int = 7,
) {
    val heights = remember(barCount, seed) { waveformHeights(barCount, seed) }
    val phase = if (animated) {
        val transition = rememberInfiniteTransition(label = "wave")
        val p by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "wavePhase",
        )
        p
    } else null

    Canvas(modifier) {
        val slot = size.width / barCount
        val barW = (slot * 0.55f).coerceAtLeast(1.5f)
        for (i in 0 until barCount) {
            val base = heights[i]
            val h = if (phase != null) {
                base * (0.35f + 0.65f * abs(sin(phase + i * 0.55f)))
            } else base
            val barH = (size.height * h).coerceAtLeast(barW)
            val x = i * slot + (slot - barW) / 2
            val fraction = if (barCount == 1) 0f else i / (barCount - 1f)
            val played = fraction <= progress
            val color = when {
                !played && inactiveColor != null -> inactiveColor
                colors.size == 1 -> colors.first()
                else -> {
                    val pos = fraction * (colors.size - 1)
                    val idx = pos.toInt().coerceAtMost(colors.size - 2)
                    lerp(colors[idx], colors[idx + 1], pos - idx)
                }
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(x, (size.height - barH) / 2),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2),
            )
        }
    }
}

/**
 * Audio row: play button + waveform with animated playback progress.
 * Real audio would come from a URL; here [onPlay] speaks via TTS.
 */
@Composable
fun AudioPlayerRow(
    durationSeconds: Int,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
    waveColor: Color = PrimaryDark,
    inactiveWaveColor: Color = OutlineGray,
    buttonColor: Color = Primary,
    seed: Int = 3,
    playButtonFirst: Boolean = false,
    labelColor: Color = TextSecondary,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val playing = progress.isRunning

    val playButton: @Composable () -> Unit = {
        Box(
            Modifier
                .size(40.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable(role = Role.Button) {
                    scope.launch {
                        if (playing) {
                            progress.stop()
                            progress.snapTo(0f)
                        } else {
                            onPlay()
                            progress.snapTo(0f)
                            progress.animateTo(1f, tween(durationSeconds.coerceAtLeast(1) * 1000, easing = LinearEasing))
                            progress.snapTo(0f)
                        }
                    }
                }
                .semantics { contentDescription = if (playing) "থামান" else "চালান" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = SurfaceWhite,
            )
        }
    }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (playButtonFirst) {
            playButton()
            Spacer(Modifier.width(10.dp))
        }
        WaveformBars(
            modifier = Modifier.weight(1f).height(30.dp),
            barCount = 40,
            colors = listOf(waveColor),
            inactiveColor = inactiveWaveColor,
            progress = if (playing) progress.value else 1f,
            seed = seed,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = trailingLabel ?: String.format(Locale.US, "0:%02d", durationSeconds),
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
        )
        if (!playButtonFirst) {
            Spacer(Modifier.width(10.dp))
            playButton()
        }
    }
}

/** Big circular gauge with Bengali percentage in the middle. */
@Composable
fun ScoreGauge(
    percent: Int,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    color: Color = PrimaryLight,
    textColor: Color = SurfaceWhite,
) {
    val animated by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "gauge",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator(
            progress = { animated },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 8.dp,
            trackColor = Color.White.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round,
        )
        Text(
            text = "${(animated * 100).toInt().toBengaliDigits()}%",
            color = textColor,
            fontSize = (size.value * 0.24f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------------------------------------------------------------------------
// Illustrations used on cards
// ---------------------------------------------------------------------------

/** Open Qaida book with floating Arabic letters (home hero card). */
@Composable
fun QaidaBookIllustration(modifier: Modifier = Modifier, size: Dp = 76.dp) {
    Box(modifier.size(size)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = this.size.minDimension / 100f
            val cream = Color(0xFFFFF4D6)
            val edge = SecondaryDark
            val left = Path().apply {
                moveTo(8f * u, 52f * u); lineTo(48f * u, 60f * u); lineTo(48f * u, 96f * u); lineTo(8f * u, 88f * u); close()
            }
            val right = Path().apply {
                moveTo(92f * u, 52f * u); lineTo(52f * u, 60f * u); lineTo(52f * u, 96f * u); lineTo(92f * u, 88f * u); close()
            }
            drawRoundRect(PrimaryDark, Offset(4f * u, 86f * u), Size(92f * u, 10f * u), CornerRadius(3f * u))
            drawPath(left, cream); drawPath(left, edge, style = Stroke(1.2f * u))
            drawPath(right, cream); drawPath(right, edge, style = Stroke(1.2f * u))
            for (k in 0..2) {
                val y = 68f + k * 8f
                drawLine(edge.copy(alpha = 0.4f), Offset(14f * u, y * u), Offset(42f * u, (y + 5f) * u), 1.2f * u)
                drawLine(edge.copy(alpha = 0.4f), Offset(58f * u, (y + 5f) * u), Offset(86f * u, y * u), 1.2f * u)
            }
            drawCircle(Primary, 6f * u, Offset(30f * u, 56f * u))
            drawCircle(Secondary, 6f * u, Offset(70f * u, 56f * u))
        }
        Row(
            Modifier.align(Alignment.TopCenter).padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("ا", color = Secondary, fontSize = (size.value * 0.26f).sp, fontWeight = FontWeight.Bold)
            Text("ب", color = PrimaryLight, fontSize = (size.value * 0.26f).sp, fontWeight = FontWeight.Bold)
            Text("ا", color = Secondary, fontSize = (size.value * 0.26f).sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Gold trophy surrounded by colourful question badges (quiz card). */
@Composable
fun TrophyIllustration(modifier: Modifier = Modifier, size: Dp = 84.dp) {
    Box(modifier.size(size)) {
        Icon(
            Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = Secondary,
            modifier = Modifier.align(Alignment.Center).size(size * 0.66f),
        )
        QuestionBubble(SecondaryDark, size * 0.34f, Modifier.align(Alignment.TopEnd))
        QuestionBubble(Secondary, size * 0.24f, Modifier.align(Alignment.TopStart).offset(x = 8.dp))
        QuestionBubble(Primary, size * 0.24f, Modifier.align(Alignment.CenterStart).offset(y = 8.dp))
        QuestionBubble(PrimaryLight, size * 0.24f, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun QuestionBubble(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text("?", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.6f).sp)
    }
}

/** Hexagonal achievement shield with crown and ribbon label. */
@Composable
fun AchievementBadge(
    title: String,
    colors: List<Color>,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
) {
    Box(
        modifier
            .size(width = size, height = size)
            .alpha(if (unlocked) 1f else 0.4f)
            .semantics { contentDescription = title + if (unlocked) " অর্জিত" else " এখনও অর্জিত হয়নি" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val hex = Path().apply {
                moveTo(w * 0.5f, h * 0.04f)
                lineTo(w * 0.88f, h * 0.24f)
                lineTo(w * 0.88f, h * 0.66f)
                lineTo(w * 0.5f, h * 0.9f)
                lineTo(w * 0.12f, h * 0.66f)
                lineTo(w * 0.12f, h * 0.24f)
                close()
            }
            drawPath(hex, Brush.verticalGradient(colors))
            drawPath(hex, GoldBrush, style = Stroke(width = 3.dp.toPx()))
            // Crown
            val crown = Path().apply {
                moveTo(w * 0.30f, h * 0.48f)
                lineTo(w * 0.28f, h * 0.24f)
                lineTo(w * 0.40f, h * 0.36f)
                lineTo(w * 0.50f, h * 0.18f)
                lineTo(w * 0.60f, h * 0.36f)
                lineTo(w * 0.72f, h * 0.24f)
                lineTo(w * 0.70f, h * 0.48f)
                close()
            }
            drawPath(crown, Brush.verticalGradient(listOf(SecondaryLight, Secondary)))
            drawCircle(SecondaryDark, w * 0.035f, Offset(w * 0.5f, h * 0.38f))
            for (x in listOf(0.28f, 0.5f, 0.72f)) {
                drawCircle(SecondaryLight, w * 0.025f, Offset(w * x, h * (if (x == 0.5f) 0.18f else 0.24f)))
            }
        }
        Text(
            text = "★ $title ★",
            color = SurfaceWhite,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (size.value * 0.12f).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(PrimaryDark, Primary, PrimaryDark)))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/** Small surface that shows an empty/loading/info state inside a card. */
@Composable
fun InfoStrip(text: String, modifier: Modifier = Modifier, color: Color = PrimaryContainer) {
    Surface(color = color, shape = RoundedCornerShape(10.dp), modifier = modifier.fillMaxWidth()) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}
