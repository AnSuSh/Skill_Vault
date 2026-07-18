package com.quickthought.skillvault.domain.model

import com.quickthought.skillvault.data.local.CredentialEntity


data class CredentialItemUI(
    val credentialId: Int,
    val accountName: String,
    val username: String,
    val websiteUrl: String? = null,
    val packageName: String? = null
    // Password field is intentionally omitted for security and encapsulation.
)

/**
 * Maps the CredentialEntity from the database to the secure domain model.
 * The domain model must *not* expose the sensitive encrypted password.
 */
fun CredentialEntity.toDomainModel(): CredentialItemUI {
    return CredentialItemUI(
        credentialId = this.id,
        accountName = this.accountName,
        username = this.username,
        websiteUrl = this.websiteUrl,
        packageName = this.packageName
    )
}

/**
 * Maps the domain model back to the database entity.
 */
fun CredentialItemUI.toEntity(encryptedPassword: String): CredentialEntity {
    return CredentialEntity(
        id = this.credentialId,
        accountName = this.accountName,
        username = this.username,
        encryptedPassword = encryptedPassword,
        websiteUrl = this.websiteUrl,
        packageName = this.packageName
    )
}
