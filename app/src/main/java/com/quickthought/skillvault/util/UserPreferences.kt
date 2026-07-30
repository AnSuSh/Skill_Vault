package com.quickthought.skillvault.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    val presetName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PRESET_NAME_KEY]
    }

    suspend fun updatePresetName(name: String) {
        dataStore.edit { preferences ->
            preferences[PRESET_NAME_KEY] = name
        }
    }

    companion object {
        private val PRESET_NAME_KEY = stringPreferencesKey("preset_name")
    }
}
