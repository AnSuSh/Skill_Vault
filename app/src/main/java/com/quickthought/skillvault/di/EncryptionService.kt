package com.quickthought.skillvault.di

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A service to handle encryption and decryption of strings using the Android Keystore system.
 * This class is designed to be a singleton managed by Hilt and uses the modern Keystore API.
 */
@Singleton
class EncryptionService @Inject constructor(private val secretKey: SecretKey) {

    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    /**
     * Encrypts a plain text string.
     * @param plaintext The string to encrypt.
     * @return The encrypted data as a Base64 encoded string.
     */
    fun encrypt(plaintext: String): String {
        // 1. Generate a new Initialization Vector (IV) for each encryption
        val iv = ByteArray(12) // GCM standard IV size is 12 bytes
        SecureRandom().nextBytes(iv)
        val gcmParamSpec = GCMParameterSpec(128, iv) // 128 bit auth tag size

        // 2. Initialize the cipher for encryption
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec)

        // 3. Encrypt the data
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

        // 4. Combine IV and encrypted data to store together, then Base64 encode
        val combined = iv + encryptedBytes
        return android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
    }

    /**
     * Decrypts a Base64 encoded string.
     * @param encryptedText The Base64 encoded string to decrypt.
     * @return The original plaintext string.
     */
    fun decrypt(encryptedText: String): String {
        // 1. Decode the Base64 string and split it back into IV and encrypted data
        val combined = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT)
        val iv = combined.copyOfRange(0, 12)
        val encryptedBytes = combined.copyOfRange(12, combined.size)

        // 2. Create the GCM parameter spec from the extracted IV
        val gcmParamSpec = GCMParameterSpec(128, iv)

        // 3. Initialize the cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec)

        // 4. Decrypt the data
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        // 5. Return the decrypted data as a string
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}

// Gemini Answer

/*
*
* package com.skillvault.data.security

import javax.inject.Inject
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import android.util.Base64

/**
 * A service to handle the encryption/decryption of passwords before/after
 * they are stored in the database.
 * NOTE: This uses a simple AES implementation for demonstration.
 */
class EncryptionService @Inject constructor(
    private val encryptionKey: String // Injected from SecurityModule
) {
    // Standard AES settings
    private val transformation = "AES/CBC/PKCS5Padding"
    private val algorithm = "AES"

    // Key Specification: Uses the securely stored UUID string as the key
    private val secretKey = SecretKeySpec(encryptionKey.toByteArray(), algorithm)

    // Initialization Vector (IV): Essential for security. Using a simple, fixed IV
    // for this demonstration is weak, but demonstrates the concept.
    // A production app should generate a unique IV for *every* encryption and store it
    // alongside the ciphertext.
    private val iv = IvParameterSpec(ByteArray(16)) // Fixed 16-byte IV for simplicity

    fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            // Encode the result to a URL-safe Base64 string for database storage
            return Base64.encodeToString(cipherText, Base64.DEFAULT)
        } catch (e: Exception) {
            // In a real app, log and handle this error gracefully
            throw SecurityException("Encryption failed", e)
        }
    }

    fun decrypt(encryptedText: String): String {
        try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT))
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // In a real app, log and handle this error silently (or show a generic error)
            throw SecurityException("Decryption failed", e)
        }
    }
}
* */
