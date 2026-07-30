package com.quickthought.skillvault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stored address for autofill.
 */
@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String, // e.g., "Home", "Work"
    val fullName: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val email: String? = null,
    val phone: String? = null,
    val dateCreated: Long = System.currentTimeMillis()
)
