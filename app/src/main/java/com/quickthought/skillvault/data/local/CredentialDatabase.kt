package com.quickthought.skillvault.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main database class for the SkillVault application.
 * Version 1 is the initial release.
 */
@Database(
    entities = [CredentialEntity::class, AddressEntity::class, CreditCardEntity::class, EmailEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class CredentialDatabase: RoomDatabase() {
    // Define the DAOs that the database contains
    abstract fun credentialDao(): CredentialDAO
    abstract fun addressDao(): AddressDAO
    abstract fun creditCardDao(): CreditCardDAO
    abstract fun emailDao(): EmailDAO

    companion object {
        const val DATABASE_NAME = "skill_vault_db"
    }
}