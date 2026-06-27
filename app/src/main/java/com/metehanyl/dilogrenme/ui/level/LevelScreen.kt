package com.metehanyl.dilogrenme.ui.level

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.data.WordBank
import com.metehanyl.dilogrenme.data.WordEntry
import com.metehanyl.dilogrenme.ui.theme.BadRed
import com.metehanyl.dilogrenme.ui.theme.GoodGreen
import kotlin.random.Random

private enum class LevelStep { CHOOSE, TEST, RESULT }

private data class PlacementQuestion(
    val tier: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun LevelScreen(
    strings: UiStrings,
    wordBank: WordBank,
    nativeLang: String,
    targetLang: String,
    currentTier: Int,
    onTierChosen: (Int) -> Unit,
    onBack: (() -> Unit)?
) {
    var step by remember { mutableStateOf(LevelStep.CHOOSE) }
    val questions = remember(wordBank, nativeLang, targetLang) {
        buildPlacementQuestions(wordBank.words, nativeLang, targetLang)
    }
    var questionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var scorePerTier by remember { mutableStateOf(mapOf<Int, Int>()) }

    fun resetTest() {
        questionIndex = 0
        selectedOption = null
        scorePerTier = mapOf()
        step = LevelStep.TEST
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                }
            }
            when (step) {
                LevelStep.CHOOSE -> ChooseStep(
                    strings = strings,
                    currentTier = currentTier,
                    onManualSelect = onTierChosen,
                    onStartTest = { resetTest() }
                )

                LevelStep.TEST -> {
                    val question = questions[questionIndex]
                    TestStep(
                        strings = strings,
                        question = question,
                        questionNumber = questionIndex + 1,
                        totalQuestions = questions.size,
                        selectedOption = selectedOption,
                        onSelect = { index ->
                            if (selectedOption == null) {
                                selectedOption = index
                                if (index == question.correctIndex) {
                                    val updated = (scorePerTier[question.tier] ?: 0) + 1
                                    scorePerTier = scorePerTier + (question.tier to updated)
                                }
                            }
                        },
                        onNext = {
                            if (questionIndex + 1 < questions.size) {
                                questionIndex += 1
                                selectedOption = null
                            } else {
                                step = LevelStep.RESULT
                            }
                        }
                    )
                }

                LevelStep.RESULT -> {
                    val recommended = recommendTier(scorePerTier)
                    ResultStep(
                        strings = strings,
                        recommendedTier = recommended,
                        onUse = { onTierChosen(recommended) },
                        onRetry = { resetTest() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseStep(
    strings: UiStrings,
    currentTier: Int,
    onManualSelect: (Int) -> Unit,
    onStartTest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = strings.levelScreenTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = strings.levelIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Button(
            onClick = onStartTest,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(strings.levelTestCta)
        }

        Text(
            text = strings.levelChooseManual,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 28.dp, bottom = 10.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..5).forEach { tier ->
                TierCard(
                    tier = tier,
                    name = tierName(strings, tier),
                    selected = tier == currentTier,
                    onClick = { onManualSelect(tier) }
                )
            }
        }
    }
}

@Composable
private fun TierCard(tier: Int, name: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$tier · $name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TestStep(
    strings: UiStrings,
    question: PlacementQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${strings.levelTestQuestion} $questionNumber/$totalQuestions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        LinearProgressIndicator(
            progress = { questionNumber / totalQuestions.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        Text(
            text = question.prompt,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctIndex
                val isSelected = index == selectedOption
                val revealed = selectedOption != null

                val containerColor = when {
                    revealed && isCorrect -> GoodGreen.copy(alpha = 0.18f)
                    revealed && isSelected && !isCorrect -> BadRed.copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor: Color? = when {
                    revealed && isCorrect -> GoodGreen
                    revealed && isSelected && !isCorrect -> BadRed
                    else -> null
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = borderColor?.let { BorderStroke(2.dp, it) },
                    onClick = { onSelect(index) }
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    )
                }
            }
        }

        if (selectedOption != null) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(strings.next)
            }
        }
    }
}

@Composable
private fun ResultStep(
    strings: UiStrings,
    recommendedTier: Int,
    onUse: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.levelResultTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Text(
            text = tierName(strings, recommendedTier),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Button(
            onClick = onUse,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(strings.levelResultUse)
        }
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text(strings.levelRetryTest)
        }
    }
}

private fun tierName(strings: UiStrings, tier: Int): String = when (tier) {
    1 -> strings.tier1Name
    2 -> strings.tier2Name
    3 -> strings.tier3Name
    4 -> strings.tier4Name
    else -> strings.tier5Name
}

private fun textOf(word: WordEntry, lang: String): String =
    word.text[lang] ?: word.text["en"] ?: word.id

private fun buildPlacementQuestions(
    words: List<WordEntry>,
    nativeLang: String,
    targetLang: String
): List<PlacementQuestion> {
    val rng = Random(System.currentTimeMillis())
    val questions = mutableListOf<PlacementQuestion>()
    for (tier in 1..5) {
        val tierWords = words.filter { it.tier == tier }.shuffled(rng).take(2)
        tierWords.forEach { word ->
            val correctText = textOf(word, nativeLang)
            val distractors = words.filter { it.id != word.id }
                .map { textOf(it, nativeLang) }
                .filter { it != correctText }
                .distinct()
                .shuffled(rng)
                .take(2)
            val options = (distractors + correctText).shuffled(rng)
            questions.add(
                PlacementQuestion(
                    tier = tier,
                    prompt = textOf(word, targetLang),
                    options = options,
                    correctIndex = options.indexOf(correctText)
                )
            )
        }
    }
    return questions
}

private fun recommendTier(scorePerTier: Map<Int, Int>): Int {
    var recommended = 1
    for (tier in 1..5) {
        if ((scorePerTier[tier] ?: 0) >= 1) {
            recommended = tier
        } else {
            break
        }
    }
    return recommended
}
