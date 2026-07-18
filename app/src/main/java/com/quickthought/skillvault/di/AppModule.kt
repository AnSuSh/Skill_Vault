package com.quickthought.skillvault.di

import android.content.Context
import android.view.autofill.AutofillManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAutofillManager(@ApplicationContext context: Context): AutofillManager? {
        return context.getSystemService(AutofillManager::class.java)
    }
}
