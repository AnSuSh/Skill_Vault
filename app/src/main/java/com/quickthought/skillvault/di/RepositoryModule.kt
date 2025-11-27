package com.quickthought.skillvault.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Provides a singleton instance of the CredentialRepository.
     * Hilt automatically knows how to construct CredentialRepository because
     * its dependencies (DAO, EncryptionService) are provided elsewhere.
     */
//    @Binds
//    @Singleton
//    abstract fun bindCredentialRepository(
//        repository: CredentialRepository
//    ): CredentialRepository
}