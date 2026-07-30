package com.quickthought.skillvault.util

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityGenerator @Inject constructor() {

    private val random = SecureRandom()

    fun generateUsername(presetName: String? = null): String {
        val base = presetName ?: "User"
        val suffix = random.nextInt(10000).toString().padStart(4, '0')
        return "$base$suffix"
    }

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        return PasswordGenerator.generate(length, includeUpper, includeLower, includeDigits, includeSymbols)
    }
}
