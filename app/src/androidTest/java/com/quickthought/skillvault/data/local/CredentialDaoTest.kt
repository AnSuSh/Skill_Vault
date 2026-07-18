package com.quickthought.skillvault.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialDaoTest {

    private lateinit var database: CredentialDatabase
    private lateinit var dao: CredentialDAO

    @Before
    fun setup() {
        // Use an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CredentialDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.credentialDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetCredential() = runTest {
        val credential = CredentialEntity(
            accountName = "Google",
            username = "user@gmail.com",
            encryptedPassword = "encrypted_pass"
        )
        dao.insertCredential(credential)

        val allCredentials = dao.getAllCredentials().first()
        assertEquals(1, allCredentials.size)
        assertEquals("Google", allCredentials[0].accountName)
    }

    @Test
    fun deleteCredential() = runTest {
        val credential = CredentialEntity(
            id = 1,
            accountName = "Google",
            username = "user@gmail.com",
            encryptedPassword = "encrypted_pass"
        )
        dao.insertCredential(credential)
        dao.deleteCredential(credential)

        val result = dao.getCredentialById(1)
        assertNull(result)
    }
}
