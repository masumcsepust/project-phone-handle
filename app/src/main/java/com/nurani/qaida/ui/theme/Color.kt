package com.nurani.qaida.ui.theme

import androidx.compose.ui.graphics.Color

// The app uses exactly two brand colours — a green primary and a gold
// secondary — each in dark / base / light shades, plus neutrals.

// Primary (green)
val PrimaryDark = Color(0xFF0F5132)
val Primary = Color(0xFF15803D)
val PrimaryLight = Color(0xFF4ADE80)
val PrimaryContainer = Color(0xFFECFDF5)

// Secondary (gold)
val SecondaryDark = Color(0xFFB45309)
val Secondary = Color(0xFFF59E0B)
val SecondaryLight = Color(0xFFFDE68A)
val SecondaryContainer = Color(0xFFFFFBEB)

// Neutrals
val SurfaceWhite = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFBFE6CF)
val StripGray = Color(0xFFF1F3F2)
val DividerGray = Color(0xFFE5E7EB)
val OutlineGray = Color(0xFFCBD5E1)
val TextPrimary = Color(0xFF1F2A24)
val TextSecondary = Color(0xFF5B6B62)

/** Voice waveform: primary → secondary blend. */
val WaveformColors = listOf(PrimaryLight, SecondaryLight, Secondary)
