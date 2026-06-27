package com.metehanyl.dilogrenme.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metehanyl.dilogrenme.data.QuizQuestion
import com.metehanyl.dilogrenme.data.UiStrings
import com.metehanyl.dilogrenme.ui.theme.BadRed
import com.metehanyl.dilogrenme.ui.theme.GoodGreen

@Composable
fun QuizQuestionView(
    strings: UiStrings,
    question: QuizQuestion,
    questionLabel: String,
    showPromptText: Boolean,
    selectedIndex: Int?,
    streak: Int,
    tier: Int,
    onSelect: (Int) -> Unit,
    onSpeak: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text("${strings.level} $tier", style = MaterialTheme.typography.labelMedium)
                    LinearProgressIndicator(
                        progress = { (streak % 5) / 5f },
                        modifier = Modifier.width(120.dp).height(6.dp).padding(top = 2.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = questionLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .size(96.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(72.dp)) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = strings.listen,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    if (showPromptText) {
                        Text(
                            text = question.prompt,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        Text(
                            text = strings.listeningHint,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { index, option ->
                    val isCorrect = index == question.correctIndex
                    val isSelected = index == selectedIndex
                    val revealed = selectedIndex != null

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
                        border = borderColor?.let { androidx.compose.foundation.BorderStroke(2.dp, it) },
                        onClick = { if (selectedIndex == null) onSelect(index) }
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = selectedIndex != null) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    val correct = selectedIndex == question.correctIndex
                    Text(
                        text = if (correct) strings.correct else "${strings.incorrect} · ${strings.correctAnswerWas} ${question.options[question.correctIndex]}",
                        color = if (correct) GoodGreen else BadRed,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(strings.next)
                    }
                }
            }
        }
    }
}
