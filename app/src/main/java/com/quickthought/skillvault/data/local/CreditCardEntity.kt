package com.quickthought.skillvault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stored credit card for autofill.
 * Sensitive fields are stored encrypted.
 */
@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String, // e.g., "Main Visa"
    val cardholderName: String,
    val encryptedCardNumber: String,
    val expiryMonth: String, // MM
    val expiryYear: String,  // YYYY
    val encryptedCvv: String,
    val brand: String? = null, // e.g., "Visa", "Mastercard"
    val dateCreated: Long = System.currentTimeMillis()
)
