package com.quickthought.skillvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
* DAO for performing CRUD operations on the CredentialEntity.
*/
@Dao
interface CredentialDAO {
    /**
     * Retrieves all credentials, ordered by the creation date.
     * Returned as a Flow to emit new values when the database changes.
     */
    @Query("SELECT * FROM credentials ORDER BY dateCreated DESC")
    fun getAllCredentials(): Flow<List<CredentialEntity>>

    /**
     * Retrieves a single credential by ID.
     */
    @Query("SELECT * FROM credentials WHERE id = :credentialId LIMIT 1")
    suspend fun getCredentialById(credentialId: Int): CredentialEntity?

    /**
     * Inserts a new credential. If the primary key conflicts (shouldn't happen with auto-generate),
     * it will replace the old entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: CredentialEntity)

    /**
     * Updates an existing credential (match by ID).
     */
    @Update
    suspend fun updateCredential(credential: CredentialEntity)

    /**
     * Deletes a specific credential.
     */
    @Delete
    suspend fun deleteCredential(credential: CredentialEntity)
}