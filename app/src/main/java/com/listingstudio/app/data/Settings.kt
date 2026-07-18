package com.listingstudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Persists the user's Gemini API key locally on the device. */
class Settings(private val context: Context) {

    private val keyApi = stringPreferencesKey("gemini_api_key")

    val apiKey: Flow<String> = context.dataStore.data.map { it[keyApi] ?: "" }

    suspend fun setApiKey(value: String) {
        context.dataStore.edit { it[keyApi] = value.trim() }
    }
}
