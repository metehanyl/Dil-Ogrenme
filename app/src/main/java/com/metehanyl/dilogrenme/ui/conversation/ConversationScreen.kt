package com.metehanyl.dilogrenme.ui.conversation

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.ai.AiConversationClient
import com.metehanyl.dilogrenme.ai.ChatRequestMessage
import com.metehanyl.dilogrenme.data.AppLanguage
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.speech.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    strings: UiStrings,
    nativeLang: AppLanguage,
    targetLang: AppLanguage,
    apiKey: String,
    apiBaseUrl: String,
    apiModel: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { AiConversationClient() }

    val messages = remember { mutableStateListOf<ChatRequestMessage>() }
    var input by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val systemPrompt = remember(nativeLang, targetLang) {
        "You are a warm, patient conversation partner helping a learner practice " +
            "${targetLang.nativeName}. Speak ONLY in ${targetLang.nativeName} (locale ${targetLang.localeTag}), " +
            "using short, simple, everyday sentences (1-3 sentences per reply) suited to a learner. " +
            "If the learner makes a mistake, gently include the corrected phrase in your reply. " +
            "Ask a friendly follow-up question to keep the conversation going. The learner's native " +
            "language is ${nativeLang.nativeName} - only switch to it if they explicitly ask for a translation " +
            "or seem completely stuck."
    }

    fun send(text: String) {
        if (text.isBlank() || isSending) return
        messages.add(ChatRequestMessage("user", text))
        input = ""
        isSending = true
        errorText = null
        scope.launch {
            val result = client.sendChat(
                baseUrl = apiBaseUrl,
                apiKey = apiKey,
                model = apiModel,
                systemPrompt = systemPrompt,
                history = messages.toList()
            )
            isSending = false
            result.onSuccess { reply ->
                messages.add(ChatRequestMessage("assistant", reply))
                TtsManager.speak(reply, targetLang.localeTag)
            }.onFailure { e ->
                errorText = e.message
            }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!text.isNullOrBlank()) send(text)
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLang.localeTag)
            }
            runCatching { speechLauncher.launch(intent) }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

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
                    text = "${strings.conversationTitle} ${targetLang.flag}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (apiKey.isBlank()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.conversationApiKeyMissing, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Button(onClick = onOpenSettings, modifier = Modifier.padding(top = 16.dp)) {
                            Text(strings.settingsTitle)
                        }
                    }
                }
                return@Column
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        targetLocale = targetLang.localeTag,
                        listenLabel = strings.conversationSpeak
                    )
                }
                if (isSending) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(strings.conversationThinking, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                if (errorText != null) {
                    item { Text(errorText.orEmpty(), color = MaterialTheme.colorScheme.error) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }) {
                    Icon(Icons.Filled.Mic, contentDescription = strings.conversationSpeak)
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(strings.conversationPlaceholder) },
                    singleLine = true
                )
                IconButton(onClick = { send(input) }, enabled = input.isNotBlank() && !isSending) {
                    Icon(Icons.Filled.Send, contentDescription = strings.conversationSend)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatRequestMessage, targetLocale: String, listenLabel: String) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(message.content, modifier = Modifier.weight(1f, fill = false))
                if (!isUser) {
                    IconButton(
                        onClick = { TtsManager.speak(message.content, targetLocale) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = listenLabel, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
