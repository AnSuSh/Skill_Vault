package com.quickthought.skillvault.util

import kotlin.math.ln
import kotlin.math.pow

object PasswordGenerator {
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+"

    fun generate(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val charPool = StringBuilder().apply {
            if (includeUpper) append(UPPER)
            if (includeLower) append(LOWER)
            if (includeDigits) append(DIGITS)
            if (includeSymbols) append(SYMBOLS)
        }.toString().ifEmpty { LOWER }

        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }

    fun calculateStrength(password: String): Float {
        if (password.isEmpty()) return 0f
        
        var score = 0f
        // Length contribution (up to 0.4)
        score += (password.length.coerceIn(0, 20) / 20f) * 0.4f
        
        // Variety contributions
        if (password.any { it.isUpperCase() }) score += 0.15f
        if (password.any { it.isLowerCase() }) score += 0.15f
        if (password.any { it.isDigit() }) score += 0.15f
        if (password.any { SYMBOLS.contains(it) }) score += 0.15f
        
        return score.coerceIn(0f, 1f)
    }

    fun estimateCrackTime(password: String): Pair<String, String> {
        if (password.isEmpty()) return "Unknown" to "No password generated"

        val charsetSize = when {
            password.any { it.isLetter() } && password.any { it.isDigit() } && password.any { "!@#$%^&*()".contains(it) } -> 94
            password.any { it.isLetter() } && password.any { it.isDigit() } -> 62
            else -> 26
        }

        // Entropy formula: log2(charsetSize^length)
        val entropy = password.length * ln(charsetSize.toDouble()) / ln(2.0)
        val combinations = 2.0.pow(entropy)
        val secondsToCrack = combinations / 100_000_000_000 // 100 Billion guesses/sec

        return when {
            secondsToCrack < 1 -> "Instant" to "A script could crack this in milliseconds."
            secondsToCrack < 60 -> "${secondsToCrack.toInt()} seconds" to "Brute force would bypass this quickly."
            secondsToCrack < 3600 -> "${(secondsToCrack / 60).toInt()} minutes" to "Standard security, but not unbreakable."
            secondsToCrack < 86400 -> "${(secondsToCrack / 3600).toInt()} hours" to "Decent protection for casual accounts."
            secondsToCrack < 31536000 -> "${(secondsToCrack / 86400).toInt()} days" to "Strong enough for most personal uses."
            secondsToCrack < 3153600000 -> "${(secondsToCrack / 31536000).toInt()} years" to "Excellent! This would take a lifetime to crack."
            else -> "Centuries" to "Legendary! Your data is safe for generations."
        }
    }
}
