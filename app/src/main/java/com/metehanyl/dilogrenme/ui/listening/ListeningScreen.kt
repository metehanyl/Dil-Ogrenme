package com.metehanyl.dilogrenme.ui.listening

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.metehanyl.dilogrenme.data.QuizEngine
import com.metehanyl.dilogrenme.data.QuizMode
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.data.WordBank
import com.metehanyl.dilogrenme.speech.TtsManager
import com.metehanyl.dilogrenme.ui.quiz.QuizQuestionView

@Composable
fun ListeningScreen(
    strings: UiStrings,
    wordBank: WordBank,
    nativeLang: String,
    targetLang: String,
    startTier: Int,
    onTierReached: (Int) -> Unit,
    onXpEarned: (Int) -> Unit,
    onBack: () -> Unit
) {
    val engine = remember(nativeLang, targetLang, startTier) {
        QuizEngine(wordBank.words, wordBank.languages, nativeLang, targetLang, startTier)
    }
    var question by remember(engine) {
        mutableStateOf(engine.nextQuestion(QuizMode.LISTENING))
    }
    var selectedIndex by remember(question) { mutableStateOf<Int?>(null) }

    LaunchedEffect(question) {
        TtsManager.speak(question.speakText, question.speakLocaleTag)
    }

    QuizQuestionView(
        strings = strings,
        question = question,
        questionLabel = strings.listeningQuestion,
        showPromptText = false,
        selectedIndex = selectedIndex,
        streak = engine.streak,
        tier = engine.tier,
        onSelect = { index ->
            selectedIndex = index
            val correct = index == question.correctIndex
            engine.recordAnswer(correct, question.word.id)
            onTierReached(engine.tier)
            if (correct) onXpEarned(10 + engine.tier * 2)
        },
        onSpeak = { TtsManager.speak(question.speakText, question.speakLocaleTag) },
        onNext = {
            question = engine.nextQuestion(QuizMode.LISTENING)
            selectedIndex = null
        },
        onBack = onBack
    )
}
