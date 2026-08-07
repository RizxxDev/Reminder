package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val H2_NOTIFICATION_ENABLED = booleanPreferencesKey("h2_notification_enabled")
    }

    val h2NotificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[H2_NOTIFICATION_ENABLED] ?: true
        }

    suspend fun setH2NotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[H2_NOTIFICATION_ENABLED] = enabled
        }
    }
}
