package com.quickthought.skillvault.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.SecureRandom
import java.util.Locale

class PassphraseGenerator(private val context: Context) {

    // Load words from assets exactly once into memory
    private val wordList: List<String> by lazy {
        val words = mutableListOf<String>()
        try {
            // Loading from standard assets folder
            context.assets.open("eff_words.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).forEachLine { line ->
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size == 2) {
                        words.add(parts[1])
                    }
                }
            }
        } catch (e: Exception) {
            VaultLogger.errorLog("Error loading words from assets", e)
        }
        words
    }

    /**
     * Generates a cryptographically secure passphrase.
     * @param wordCount Number of words in the passphrase (default 5 for high security).
     * @param separator The character used to join words (e.g., "-", " ").
     * @param capitalize Whether to capitalize each word.
     * @param includeNumber Whether to append a random digit to one of the words.
     * @return A random, memorable passphrase.
     */
    fun generatePassphrase(
        wordCount: Int = 5,
        separator: String = "-",
        capitalize: Boolean = false,
        includeNumber: Boolean = false
    ): String {
        if (wordList.isEmpty()) return ""

        val secureRandom = SecureRandom()
        val selectedWords = (1..wordCount)
            .map { wordList[secureRandom.nextInt(wordList.size)] }
            .toMutableList()

        if (capitalize) {
            for (i in selectedWords.indices) {
                selectedWords[i] = selectedWords[i].replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
        }

        if (includeNumber) {
            val randomIndex = secureRandom.nextInt(selectedWords.size)
            val randomDigit = secureRandom.nextInt(10)
            selectedWords[randomIndex] = "${selectedWords[randomIndex]}$randomDigit"
        }

        return selectedWords.joinToString(separator)
    }
}
