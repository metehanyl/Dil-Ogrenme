package com.metehanyl.dilogrenme.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dil_ogrenme_settings")

data class UserSettings(
    val nativeLang: String? = null,
    val targetLang: String? = null,
    val onboarded: Boolean = false,
    val xp: Int = 0,
    val highestTier: Int = 1,
    val selectedTier: Int = 1,
    val levelChosen: Boolean = false,
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val apiModel: String = "gpt-4o-mini"
) {
    val level: Int get() = 1 + xp / 100
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val NATIVE_LANG = stringPreferencesKey("native_lang")
        val TARGET_LANG = stringPreferencesKey("target_lang")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val XP = intPreferencesKey("xp")
        val HIGHEST_TIER = intPreferencesKey("highest_tier")
        val SELECTED_TIER = intPreferencesKey("selected_tier")
        val LEVEL_CHOSEN = booleanPreferencesKey("level_chosen")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val API_MODEL = stringPreferencesKey("api_model")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            nativeLang = prefs[Keys.NATIVE_LANG],
            targetLang = prefs[Keys.TARGET_LANG],
            onboarded = prefs[Keys.ONBOARDED] ?: false,
            xp = prefs[Keys.XP] ?: 0,
            highestTier = prefs[Keys.HIGHEST_TIER] ?: 1,
            selectedTier = prefs[Keys.SELECTED_TIER] ?: 1,
            levelChosen = prefs[Keys.LEVEL_CHOSEN] ?: false,
            apiBaseUrl = prefs[Keys.API_BASE_URL] ?: "https://api.openai.com/v1",
            apiModel = prefs[Keys.API_MODEL] ?: "gpt-4o-mini"
        )
    }

    suspend fun setLanguages(native: String, target: String) {
        context.dataStore.edit {
            it[Keys.NATIVE_LANG] = native
            it[Keys.TARGET_LANG] = target
            it[Keys.ONBOARDED] = true
        }
    }

    suspend fun setApiConfig(baseUrl: String, model: String) {
        context.dataStore.edit {
            it[Keys.API_BASE_URL] = baseUrl
            it[Keys.API_MODEL] = model
        }
    }

    suspend fun addXp(amount: Int) {
        context.dataStore.edit { it[Keys.XP] = (it[Keys.XP] ?: 0) + amount }
    }

    suspend fun saveHighestTier(tier: Int) {
        context.dataStore.edit {
            val current = it[Keys.HIGHEST_TIER] ?: 1
            if (tier > current) it[Keys.HIGHEST_TIER] = tier
        }
    }

    suspend fun setSelectedTier(tier: Int) {
        context.dataStore.edit {
            it[Keys.SELECTED_TIER] = tier
            it[Keys.LEVEL_CHOSEN] = true
            val current = it[Keys.HIGHEST_TIER] ?: 1
            if (tier > current) it[Keys.HIGHEST_TIER] = tier
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit {
            it[Keys.XP] = 0
            it[Keys.HIGHEST_TIER] = 1
            it[Keys.SELECTED_TIER] = 1
            it[Keys.LEVEL_CHOSEN] = false
        }
    }
}
