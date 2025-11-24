package com.quickthought.skillvault.data

import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.di.EncryptionService
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.domain.model.toDomainModel
import com.quickthought.skillvault.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CredentialRepository @Inject constructor(
    private val credentialDao: CredentialDAO,
    private val encryptionService: EncryptionService
) {

    /**
     * Retrieves all credentials and maps the database entities to the domain model.
     * Note: The password remains encrypted until explicitly requested for copy/view.
     */
    fun getCredentials(): Flow<List<CredentialItemUI>> {
        return credentialDao.getAllCredentials().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Saves a new or updated credential.
     * The sensitive password field is encrypted before being passed to the DAO.
     */
    suspend fun saveCredential(credential: CredentialItemUI, plainTextPassword: String) {
        val encryptedPassword = encryptionService.encrypt(plainTextPassword)
        val entity = credential.toEntity(encryptedPassword)
        credentialDao.insertCredential(entity)
    }

    /**
     * Deletes a credential by its ID.
     */
    suspend fun deleteCredential(id: Int) {
        // Since we don't have a direct delete-by-id, we fetch the entity first (or use a dedicated DAO query)
        val entityToDelete = credentialDao.getCredentialById(id)
        entityToDelete?.let { credentialDao.deleteCredential(it) }
    }

    /**
     * Retrieves a credential's password and decrypts it for display/copy.
     * THIS MUST ONLY BE CALLED AFTER BIOMETRIC AUTHENTICATION!
     */
    suspend fun getDecryptedPassword(id: Int): String {
        val entity = credentialDao.getCredentialById(id)
            ?: throw NoSuchElementException("Credential with ID $id not found.")

        // Decrypt the password string stored in the database
        return encryptionService.decrypt(entity.encryptedPassword)
    }
}