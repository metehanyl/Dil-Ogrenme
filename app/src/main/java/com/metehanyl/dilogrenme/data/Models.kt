package com.metehanyl.dilogrenme.data

import kotlinx.serialization.Serializable

@Serializable
data class AppLanguage(
    val code: String,
    val localeTag: String,
    val flag: String,
    val nativeName: String
)

@Serializable
data class WordEntry(
    val id: String,
    val category: String,
    val tier: Int,
    val text: Map<String, String>,
    val example: Map<String, String> = emptyMap()
)

@Serializable
data class WordBank(
    val languages: List<AppLanguage>,
    val words: List<WordEntry>
)

enum class QuizMode {
    WORD_TO_MEANING,
    MEANING_TO_WORD,
    LISTENING
}

data class QuizQuestion(
    val word: WordEntry,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val mode: QuizMode,
    val speakText: String,
    val speakLocaleTag: String
)
