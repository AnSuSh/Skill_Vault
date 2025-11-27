package com.quickthought.skillvault.data

import android.util.Log
import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.di.EncryptionService
import com.quickthought.skillvault.domain.model.CredentialItemUI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CredentialRepositoryTest {

    private val credentialDao: CredentialDAO = mockk()
    private val encryptionService: EncryptionService = mockk()
    private lateinit var repository: CredentialRepository

    @Before
    fun setup(){
        repository = CredentialRepository(credentialDao, encryptionService)

//        mockkStatic(Log::class)
//        every { Log.i(any(), any()) } returns 0
    }

    @Test
    fun `saveCredential should encrypt password before storing in DAO`() = runTest {
        // 1. Arrange
        val rawPassword = "secure123"
        val encryptedResult = "ENCRYPTED_DATA"
        val credential = CredentialItemUI(0, "Facebook", "user_fb")

        // We tell MockK: "When encryption is called with 'secure123', return 'ENCRYPTED_DATA'"
//        coEvery { Log.i("", "") } returns 0
        coEvery { encryptionService.encrypt(rawPassword) } returns encryptedResult
        coEvery { credentialDao.insertCredential(any()) } returns Unit

        // 2. Act
        repository.saveCredential(credential, rawPassword)

        // 3. Assert
        // Verify the encryption service was actually called with the right string
        coVerify { encryptionService.encrypt(rawPassword) }

        // Verify the DAO received the ENCRYPTED string, not the plain one
        coVerify {
            credentialDao.insertCredential(match { it.encryptedPassword == encryptedResult })
        }
    }
}