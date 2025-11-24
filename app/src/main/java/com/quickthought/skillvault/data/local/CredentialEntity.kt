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
    val dateCreated: Long = System.currentTimeMillis()
)