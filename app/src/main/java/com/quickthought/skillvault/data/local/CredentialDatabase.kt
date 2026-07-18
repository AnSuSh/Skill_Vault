package com.quickthought.skillvault.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main database class for the SkillVault application.
 * Version 1 is the initial release.
 */
@Database(
    entities = [CredentialEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class CredentialDatabase: RoomDatabase() {
    // Define the DAOs that the database contains
    abstract fun credentialDao(): CredentialDAO

    companion object {
        const val DATABASE_NAME = "skill_vault_db"
    }
}