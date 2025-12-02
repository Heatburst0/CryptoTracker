package com.example.cryptotracker.common

import android.annotation.SuppressLint
import kotlin.math.ln
import kotlin.math.pow

// Helper to format big numbers (1.2B, 3.4T)
@SuppressLint("DefaultLocale")
fun formatCompactNumber(number: Double): String {
    if (number < 1000) return String.format("%.2f", number)
    val exp = (ln(number) / ln(1000.0)).toInt()
    return String.format("%.2f %c", number / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
}