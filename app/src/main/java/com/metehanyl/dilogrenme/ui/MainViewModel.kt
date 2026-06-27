package com.metehanyl.dilogrenme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metehanyl.dilogrenme.data.ContentRepository
import com.metehanyl.dilogrenme.data.SecureKeyStore
import com.metehanyl.dilogrenme.data.SettingsRepository
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.data.UiStringsProvider
import com.metehanyl.dilogrenme.data.UserSettings
import com.metehanyl.dilogrenme.data.WordBank
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val wordBank: WordBank = ContentRepository.load(application)

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val uiStrings: UiStrings
        get() = UiStringsProvider.forLanguage(settings.value.nativeLang ?: "en")

    fun apiKey(): String = SecureKeyStore.getApiKey(getApplication())

    fun saveApiKey(key: String) {
        SecureKeyStore.setApiKey(getApplication(), key)
    }

    fun saveLanguages(native: String, target: String) {
        viewModelScope.launch { settingsRepository.setLanguages(native, target) }
    }

    fun setApiConfig(baseUrl: String, model: String) {
        viewModelScope.launch { settingsRepository.setApiConfig(baseUrl, model) }
    }

    fun addXp(amount: Int) {
        viewModelScope.launch { settingsRepository.addXp(amount) }
    }

    fun saveHighestTier(tier: Int) {
        viewModelScope.launch { settingsRepository.saveHighestTier(tier) }
    }

    fun saveSelectedTier(tier: Int) {
        viewModelScope.launch { settingsRepository.setSelectedTier(tier) }
    }

    fun resetProgress() {
        viewModelScope.launch { settingsRepository.resetProgress() }
    }
}
