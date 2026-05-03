package com.quickthought.skillvault.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    private const val PREFERENCES_FILE_NAME = "skill_vault_prefs"
    private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val SECRET_KEY_ALIAS = "_skill_vault_secret_key_"

    /**
     * Provides a singleton SecretKey instance for encryption/decryption.
     * The key is created and stored in the Android Keystore if it doesn't exist.
     */
    @Provides
    @Singleton
    fun provideSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
            load(null)
        }

        if (!keyStore.containsAlias(SECRET_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                SECRET_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        return keyStore.getKey(SECRET_KEY_ALIAS, null) as SecretKey
    }

    /**
     * Provides a singleton instance of our custom EncryptionService.
     * This service now depends on the modern SecretKey.
     */
    @Provides
    @Singleton
    fun provideEncryptionService(
        secretKey: SecretKey
    ): EncryptionService {
        return EncryptionService(secretKey)
    }

    /**
     * Provides a singleton instance of EncryptedSharedPreferences.
     * Note: EncryptedSharedPreferences still uses the deprecated MasterKey internally.
     * A fully modern alternative is not yet available in a stable library.
     * This implementation creates a separate, compatible MasterKey for it.
     */
    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val spec = KeyGenParameterSpec.Builder(
            androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFERENCES_FILE_NAME,
            masterKey, // It requires the old MasterKey
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
