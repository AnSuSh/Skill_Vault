package com.quickthought.skillvault.domain.model

import com.quickthought.skillvault.data.local.CreditCardEntity

data class CreditCardUI(
    val id: Int,
    val label: String,
    val cardholderName: String,
    val maskedCardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val brand: String? = null
)

fun CreditCardEntity.toUIModel(): CreditCardUI {
    // Basic masking logic
    val masked = if (encryptedCardNumber.length > 4) {
        "**** **** **** ${encryptedCardNumber.takeLast(4)}" // This is dummy masking since it's encrypted
    } else "****"
    
    // In reality, we'd decrypt first if we wanted the real last 4, 
    // but for the UI model we can store a placeholder or decypt it in the repo.
    return CreditCardUI(
        id = id,
        label = label,
        cardholderName = cardholderName,
        maskedCardNumber = masked, // We'll fix this in the repo mapper
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        brand = brand
    )
}

fun CreditCardUI.toEntity(encryptedCardNumber: String, encryptedCvv: String): CreditCardEntity {
    return CreditCardEntity(
        id = id,
        label = label,
        cardholderName = cardholderName,
        encryptedCardNumber = encryptedCardNumber,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        encryptedCvv = encryptedCvv,
        brand = brand
    )
}
