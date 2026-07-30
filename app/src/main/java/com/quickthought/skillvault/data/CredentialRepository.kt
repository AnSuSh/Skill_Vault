package com.quickthought.skillvault.data

import com.quickthought.skillvault.data.local.AddressDAO
import com.quickthought.skillvault.data.local.CreditCardDAO
import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.data.local.EmailDAO
import com.quickthought.skillvault.data.local.EmailEntity
import com.quickthought.skillvault.di.EncryptionService
import com.quickthought.skillvault.domain.model.AddressUI
import com.quickthought.skillvault.domain.model.CreditCardUI
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.domain.model.toDomainModel
import com.quickthought.skillvault.domain.model.toEntity
import com.quickthought.skillvault.domain.model.toUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository class that abstracts access to credential data.
 * Handles encryption and decryption of passwords using [EncryptionService].
 *
 * @property credentialDao Data Access Object for credentials.
 * @property addressDao Data Access Object for addresses.
 * @property creditCardDao Data Access Object for credit cards.
 * @property encryptionService Service for encrypting and decrypting data.
 */
class CredentialRepository @Inject constructor(
    private val credentialDao: CredentialDAO,
    private val addressDao: AddressDAO,
    private val creditCardDao: CreditCardDAO,
    private val emailDao: EmailDAO,
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

    // --- Address Operations ---

    fun getAddresses(): Flow<List<AddressUI>> {
        return addressDao.getAllAddresses().map { entities ->
            entities.map { it.toUIModel() }
        }
    }

    suspend fun saveAddress(address: AddressUI) = withContext(Dispatchers.IO) {
        addressDao.insertAddress(address.toEntity())
    }

    suspend fun deleteAddress(id: Int) = withContext(Dispatchers.IO) {
        val address = addressDao.getAddressById(id)
        address?.let { addressDao.deleteAddress(it) }
    }

    // --- Credit Card Operations ---

    fun getCreditCards(): Flow<List<CreditCardUI>> {
        return creditCardDao.getAllCreditCards().map { entities ->
            entities.map { entity ->
                val decryptedNumber = encryptionService.decrypt(entity.encryptedCardNumber)
                val masked = if (decryptedNumber.length > 4) {
                    "**** **** **** ${decryptedNumber.takeLast(4)}"
                } else decryptedNumber
                entity.toUIModel().copy(maskedCardNumber = masked)
            }
        }
    }

    suspend fun saveCreditCard(creditCard: CreditCardUI, cardNumber: String, cvv: String) = withContext(Dispatchers.IO) {
        val encryptedNumber = encryptionService.encrypt(cardNumber)
        val encryptedCvv = encryptionService.encrypt(cvv)
        creditCardDao.insertCreditCard(creditCard.toEntity(encryptedNumber, encryptedCvv))
    }

    suspend fun getDecryptedCardDetails(id: Int): Pair<String, String> = withContext(Dispatchers.IO) {
        val entity = creditCardDao.getCreditCardById(id)
            ?: throw NoSuchElementException("Card not found.")
        val number = encryptionService.decrypt(entity.encryptedCardNumber)
        val cvv = encryptionService.decrypt(entity.encryptedCvv)
        number to cvv
    }

    suspend fun deleteCreditCard(id: Int) = withContext(Dispatchers.IO) {
        val card = creditCardDao.getCreditCardById(id)
        card?.let { creditCardDao.deleteCreditCard(it) }
    }

    // --- Email Operations ---

    fun getEmails(): Flow<List<EmailEntity>> = emailDao.getAllEmails()

    suspend fun saveEmail(email: EmailEntity) = withContext(Dispatchers.IO) {
        emailDao.insertEmail(email)
    }

    suspend fun deleteEmail(email: EmailEntity) = withContext(Dispatchers.IO) {
        emailDao.deleteEmail(email)
    }
}
