package com.quickthought.skillvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDAO {
    @Query("SELECT * FROM emails ORDER BY dateCreated DESC")
    fun getAllEmails(): Flow<List<EmailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity)

    @Delete
    suspend fun deleteEmail(email: EmailEntity)
}
