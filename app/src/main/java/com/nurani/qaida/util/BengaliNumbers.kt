package com.nurani.qaida.util

import java.util.Locale

private const val BENGALI_DIGITS = "০১২৩৪৫৬৭৮৯"

/** Converts ASCII digits in this string to Bengali digits (e.g. "85%" → "৮৫%"). */
fun String.toBengaliDigits(): String = buildString(length) {
    for (c in this@toBengaliDigits) {
        append(if (c in '0'..'9') BENGALI_DIGITS[c - '0'] else c)
    }
}

fun Int.toBengaliDigits(): String = toString().toBengaliDigits()

/** Formats seconds as a zero-padded "০০:০৩" timer. */
fun formatTimerBn(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", m, s).toBengaliDigits()
}
