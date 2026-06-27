package com.metehanyl.dilogrenme.ui.onboarding

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.data.AppLanguage
import com.metehanyl.dilogrenme.data.UiStrings

@Composable
fun OnboardingScreen(
    languages: List<AppLanguage>,
    onFinished: (nativeCode: String, targetCode: String) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var nativeCode by remember { mutableStateOf<String?>(null) }

    val strings = remember(nativeCode) {
        UiStringsForOnboarding(nativeCode)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = if (step == 0) strings.onboardingNativeQuestion else strings.onboardingTargetQuestion,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.onboardingWelcome,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            val candidates = if (step == 0) languages else languages.filter { it.code != nativeCode }

            LanguagePickerList(
                languages = candidates,
                searchHint = strings.searchLanguage,
                onSelect = { lang ->
                    if (step == 0) {
                        nativeCode = lang.code
                        step = 1
                    } else {
                        onFinished(nativeCode ?: "en", lang.code)
                    }
                }
            )
        }
    }
}

private fun UiStringsForOnboarding(nativeCode: String?): UiStrings =
    com.metehanyl.dilogrenme.data.UiStringsProvider.forLanguage(nativeCode ?: "en")

@Composable
private fun LanguagePickerList(
    languages: List<AppLanguage>,
    searchHint: String,
    onSelect: (AppLanguage) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, languages) {
        if (query.isBlank()) languages
        else languages.filter { it.nativeName.contains(query, ignoreCase = true) }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(searchHint) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtered, key = { it.code }) { lang ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = { onSelect(lang) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lang.flag, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = lang.nativeName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
