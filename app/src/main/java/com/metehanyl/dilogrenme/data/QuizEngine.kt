package com.metehanyl.dilogrenme.data

import kotlin.random.Random

/**
 * Generates an effectively endless stream of quiz questions out of a finite word bank by
 * reshuffling prompts/distractors every time and adapting difficulty to the player's streak.
 * Missed words are requeued so they resurface within the next few questions (lightweight SRS).
 */
class QuizEngine(
    private val words: List<WordEntry>,
    private val languages: List<AppLanguage>,
    private val nativeLang: String,
    private val targetLang: String,
    startTier: Int = 1
) {
    private val rng = Random(System.currentTimeMillis())
    private val recentIds = mutableListOf<String>()
    private val missedQueue = mutableListOf<String>()

    var tier: Int = startTier.coerceIn(1, 5)
        private set
    var streak: Int = 0
        private set
    var xp: Int = 0
        private set

    private fun localeFor(code: String): String =
        languages.firstOrNull { it.code == code }?.localeTag ?: code

    fun recordAnswer(correct: Boolean, wordId: String) {
        if (correct) {
            streak += 1
            xp += 10 + tier * 2
            missedQueue.remove(wordId)
            if (streak > 0 && streak % 5 == 0 && tier < 5) {
                tier += 1
            }
        } else {
            streak = 0
            if (!missedQueue.contains(wordId)) missedQueue.add(wordId)
            if (tier > 1 && missedQueue.size > 4) tier -= 1
        }
    }

    fun nextQuestion(mode: QuizMode): QuizQuestion {
        val pool = words.filter { it.tier <= tier }.ifEmpty { words }
        val target = pickWord(pool)

        val resolvedMode = if (mode == QuizMode.LISTENING) QuizMode.LISTENING else mode

        val promptText: String
        val correctText: String
        val promptLocale: String

        when (resolvedMode) {
            QuizMode.MEANING_TO_WORD -> {
                promptText = textOf(target, nativeLang)
                correctText = textOf(target, targetLang)
                promptLocale = nativeLang
            }
            else -> {
                promptText = textOf(target, targetLang)
                correctText = textOf(target, nativeLang)
                promptLocale = targetLang
            }
        }

        val answerLang = if (resolvedMode == QuizMode.MEANING_TO_WORD) targetLang else nativeLang
        val distractors = buildDistractors(target, answerLang, correctText)

        val options = (distractors + correctText).shuffled(rng)
        val correctIndex = options.indexOf(correctText)

        return QuizQuestion(
            word = target,
            prompt = promptText,
            options = options,
            correctIndex = correctIndex,
            mode = resolvedMode,
            speakText = promptText,
            speakLocaleTag = localeFor(promptLocale)
        )
    }

    private fun textOf(word: WordEntry, lang: String): String =
        word.text[lang] ?: word.text["en"] ?: word.id

    private fun pickWord(pool: List<WordEntry>): WordEntry {
        if (missedQueue.isNotEmpty() && rng.nextInt(100) < 40) {
            val missedId = missedQueue.first()
            pool.firstOrNull { it.id == missedId }?.let { return it }
        }
        val candidates = pool.filterNot { recentIds.contains(it.id) }.ifEmpty { pool }
        val chosen = candidates[rng.nextInt(candidates.size)]
        recentIds.add(chosen.id)
        if (recentIds.size > 6) recentIds.removeAt(0)
        return chosen
    }

    private fun buildDistractors(target: WordEntry, lang: String, correctText: String): List<String> {
        val sameCategory = words.filter { it.id != target.id && it.category == target.category }
            .map { textOf(it, lang) }
            .filter { it != correctText }
            .distinct()
        val others = words.filter { it.id != target.id && it.category != target.category }
            .map { textOf(it, lang) }
            .filter { it != correctText }
            .distinct()
        val merged = (sameCategory.shuffled(rng) + others.shuffled(rng)).distinct()
        return merged.take(2)
    }
}
