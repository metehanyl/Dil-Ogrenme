package com.metehanyl.dilogrenme.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.data.AppLanguage
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.data.UserSettings

@Composable
fun SettingsScreen(
    strings: UiStrings,
    settings: UserSettings,
    languages: List<AppLanguage>,
    initialApiKey: String,
    onBack: () -> Unit,
    onSaveLanguages: (native: String, target: String) -> Unit,
    onSaveApiConfig: (baseUrl: String, model: String, apiKey: String) -> Unit,
    onResetProgress: () -> Unit
) {
    var nativeCode by remember(settings.nativeLang) { mutableStateOf(settings.nativeLang ?: "en") }
    var targetCode by remember(settings.targetLang) { mutableStateOf(settings.targetLang ?: "en") }
    var baseUrl by remember { mutableStateOf(settings.apiBaseUrl) }
    var model by remember { mutableStateOf(settings.apiModel) }
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var savedNotice by remember { mutableStateOf(false) }
    var resetNotice by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    fun langOf(code: String): AppLanguage? = languages.firstOrNull { it.code == code }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                }
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                SectionLabel(strings.settingsLanguages)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LanguageRow(
                            label = strings.settingsNative,
                            language = langOf(nativeCode),
                            onClick = { pickerTarget = PickerTarget.NATIVE }
                        )
                        Row(modifier = Modifier.padding(vertical = 10.dp)) {}
                        LanguageRow(
                            label = strings.settingsTarget,
                            language = langOf(targetCode),
                            onClick = { pickerTarget = PickerTarget.TARGET }
                        )
                        Button(
                            onClick = {
                                onSaveLanguages(nativeCode, targetCode)
                                savedNotice = true
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            enabled = nativeCode != targetCode,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(strings.settingsSave)
                        }
                    }
                }

                SectionLabel(strings.settingsAi, topPadding = 28.dp)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.settingsApiBaseUrl) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            label = { Text(strings.settingsApiModel) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            label = { Text(strings.settingsApiKey) },
                            singleLine = true,
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        Button(
                            onClick = {
                                onSaveApiConfig(baseUrl.trim(), model.trim(), apiKey.trim())
                                savedNotice = true
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(strings.settingsSave)
                        }
                        if (savedNotice) {
                            Text(
                                strings.settingsSaved,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                SectionLabel(strings.settingsResetProgress, topPadding = 28.dp)
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Text(strings.settingsResetProgress, color = MaterialTheme.colorScheme.error)
                }
                if (resetNotice) {
                    Text(
                        strings.settingsResetDone,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            confirmButton = {
                Button(onClick = {
                    onResetProgress()
                    showResetConfirm = false
                    resetNotice = true
                }) { Text(strings.settingsResetProgress) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) { Text(strings.back) }
            },
            text = { Text(strings.settingsResetProgress + "?") }
        )
    }

    pickerTarget?.let { target ->
        LanguagePickerDialog(
            languages = languages,
            searchHint = strings.searchLanguage,
            onSelect = { lang ->
                if (target == PickerTarget.NATIVE) nativeCode = lang.code else targetCode = lang.code
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null }
        )
    }
}

private enum class PickerTarget { NATIVE, TARGET }

@Composable
private fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp = 8.dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = topPadding, bottom = 10.dp)
    )
}

@Composable
private fun LanguageRow(label: String, language: AppLanguage?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            onClick = onClick
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(language?.flag.orEmpty(), modifier = Modifier.padding(end = 6.dp))
                Text(language?.nativeName.orEmpty(), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    languages: List<AppLanguage>,
    searchHint: String,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, languages) {
        if (query.isBlank()) languages
        else languages.filter { it.nativeName.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(searchHint) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().size(360.dp).padding(top = 10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.code }) { lang ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            onClick = { onSelect(lang) }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lang.flag)
                                }
                                Text(lang.nativeName, modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}
