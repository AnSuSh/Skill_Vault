package com.quickthought.skillvault.list

import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.di.EncryptionService
import com.quickthought.skillvault.domain.model.CredentialItemUI
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CredentialRepositoryTest {

    private val dao: CredentialDAO = mockk()
    private val encryption: EncryptionService = mockk()
    private lateinit var repository: CredentialRepository

    @Before
    fun setup() {
        repository = CredentialRepository(dao, encryption)
    }

    @Test
    fun `saveCredential should encrypt the password before calling DAO`() = runTest {
        // Arrange
        val rawPassword = "myPassword123"
        val encryptedPassword = "encrypted_hash"
        val credential = CredentialItemUI(0, "Google", "user@gmail.com")

        coEvery { encryption.encrypt(rawPassword) } returns encryptedPassword
        coEvery { dao.insertCredential(any()) } returns Unit

        // Act
        repository.saveCredential(credential, rawPassword)

        // Assert
        coVerify { encryption.encrypt(rawPassword) }
        coVerify { dao.insertCredential(match { it.encryptedPassword == encryptedPassword }) }
    }
}