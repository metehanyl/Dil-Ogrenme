package com.metehanyl.dilogrenme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.data.AppLanguage
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.data.UserSettings

@Composable
fun HomeScreen(
    strings: UiStrings,
    settings: UserSettings,
    nativeLang: AppLanguage?,
    targetLang: AppLanguage?,
    onOpenQuiz: () -> Unit,
    onOpenListening: () -> Unit,
    onOpenConversation: () -> Unit,
    onOpenLevel: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(strings.appName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(strings.homeGreeting, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = strings.settingsTitle)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(label = strings.level, value = settings.level.toString())
                StatChip(label = strings.xp, value = settings.xp.toString())
                StatChip(
                    label = "${nativeLang?.flag.orEmpty()} → ${targetLang?.flag.orEmpty()}",
                    value = targetLang?.nativeName.orEmpty()
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MenuCard(
                    icon = Icons.Filled.Quiz,
                    title = strings.menuQuiz,
                    subtitle = strings.menuQuizDesc,
                    onClick = onOpenQuiz
                )
                MenuCard(
                    icon = Icons.Filled.Headphones,
                    title = strings.menuListening,
                    subtitle = strings.menuListeningDesc,
                    onClick = onOpenListening
                )
                MenuCard(
                    icon = Icons.Filled.Chat,
                    title = strings.menuConversation,
                    subtitle = strings.menuConversationDesc,
                    onClick = onOpenConversation
                )
                MenuCard(
                    icon = Icons.Filled.BarChart,
                    title = strings.levelMenuTitle,
                    subtitle = strings.levelMenuDesc,
                    onClick = onOpenLevel
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}

@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }
    }
}
