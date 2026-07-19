package com.quickthought.skillvault.data

import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.di.EncryptionService
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.domain.model.toDomainModel
import com.quickthought.skillvault.domain.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository class that abstracts access to credential data.
 * Handles encryption and decryption of passwords using [EncryptionService].
 *
 * @property credentialDao Data Access Object for credentials.
 * @property encryptionService Service for encrypting and decrypting data.
 */
class CredentialRepository @Inject constructor(
    private val credentialDao: CredentialDAO,
    private val encryptionService: EncryptionService
) {

    /**
     * Retrieves all credentials and maps the database entities to the domain model.
     * Note: The password remains encrypted until explicitly requested for copy/view.
     */
    /**
     * Returns a flow of all credentials, mapped to domain models.
     *
     * @return A [Flow] containing a list of [CredentialItemUI].
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
    /**
     * Encrypts and saves a credential to the database.
     *
     * @param credential The credential domain model to save.
     * @param plainTextPassword The plaintext password to be encrypted.
     */
    suspend fun saveCredential(credential: CredentialItemUI, plainTextPassword: String) {
        /**
         * Remove the Log.i(...) calls from your CredentialRepository.kt file. A repository's job is to manage data, not to log.
         * If you need to debug, use the debugger or temporary println statements that you remove later.
         * */
//        Log.i("CredentialRepository", "Saving credential: $credential")
        val encryptedPassword = encryptionService.encrypt(plainTextPassword)
        val entity = credential.toEntity(encryptedPassword)
        credentialDao.insertCredential(entity)
    }

    /**
     * Deletes a credential by its ID.
     */
    /**
     * Deletes a credential from the database by its ID.
     *
     * @param id The ID of the credential to delete.
     */
    suspend fun deleteCredential(id: Int) {
        // Since we don't have a direct delete-by-id, we fetch the entity first (or use a dedicated DAO query)
        val entityToDelete = credentialDao.getCredentialById(id)
        entityToDelete?.let { credentialDao.deleteCredential(it) }
    }

    // In CredentialRepository.kt
    /**
     * Fetches a credential by ID and returns its decrypted password.
     *
     * @param id The ID of the credential.
     * @return The decrypted plaintext password.
     * @throws NoSuchElementException if the credential is not found.
     */
    suspend fun getDecryptedPassword(id: Int): String = withContext(Dispatchers.IO) {

        // 1. Fetch Encrypted Entity: Suspends while Room retrieves the data.
        val entity = credentialDao.getCredentialById(id)
            ?: throw NoSuchElementException("Credential not found.")

        // 2. Decrypt: Uses the EncryptionService with the secure MasterKey-derived key.
        val encryptedText = entity.encryptedPassword

        // 3. Return Plaintext: This is the string ready for the clipboard.
        encryptionService.decrypt(encryptedText)
    }

    suspend fun getCredentialsForAutofill(domain: String?, packageName: String?): List<CredentialItemUI> = withContext(Dispatchers.IO) {
        credentialDao.findCredentialsForAutofill(domain, packageName).map { it.toDomainModel() }
    }
}
