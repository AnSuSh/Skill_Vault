package com.quickthought.skillvault.di

import android.content.Context
import androidx.room.Room
import com.quickthought.skillvault.data.local.CredentialDAO
import com.quickthought.skillvault.data.local.CredentialDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// We install this in the Application container so the database instance is a Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the factory for SQLCipher encryption.
     * This uses a hardcoded key for *demonstration purposes only*.
     * In production, the key must be stored securely (e.g., using Android Keystore).
     */
//    @Provides
//    fun provideSupportFactory(): SupportSQLiteOpenHelper.Factory {
//        val sqlCipherKey = ByteArray(32)
//        SecureRandom().nextBytes(sqlCipherKey)
//        val passphrase = SQLiteDatabase.getBytes("your_super_secret_db_key".toCharArray())
//        return SupportSQLiteOpenHelper.Factory(passphrase)
//    }

    /**
     * Provides the single instance of the CredentialDatabase.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
//        supportFactory: SupportSQLiteOpenHelper.Factory // Inject the encrypted factory
    ): CredentialDatabase {
        return Room.databaseBuilder(
            context,
            CredentialDatabase::class.java,
            CredentialDatabase.DATABASE_NAME
        )
            // Integrates SQLCipher with Room
//            .openHelperFactory(supportFactory)
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the DAO instance for injection into the Repository.
     */
    @Provides
    fun provideCredentialDao(database: CredentialDatabase): CredentialDAO {
        return database.credentialDao()
    }
}