package com.quickthought.skillvault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val isSystemAccount: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis()
)
