package com.quickthought.skillvault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single row in the 'credentials' table.
 * NOTE: The `encryptedPassword` stores the secure, ciphered data.
 */
@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountName: String,
    val username: String,
    /** The actual encrypted password string. */
    val encryptedPassword: String,
    /** The website URL associated with this credential (e.g., https://google.com). */
    val websiteUrl: String? = null,
    /** The Android package name associated with this credential (e.g., com.google.android.gm). */
    val packageName: String? = null,
    val dateCreated: Long = System.currentTimeMillis()
)
