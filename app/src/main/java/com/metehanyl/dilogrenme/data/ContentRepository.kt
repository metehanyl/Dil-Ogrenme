package com.metehanyl.dilogrenme.data

import android.content.Context
import kotlinx.serialization.json.Json

object ContentRepository {

    private var cached: WordBank? = null

    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): WordBank {
        cached?.let { return it }
        val text = context.assets.open("words.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val bank = json.decodeFromString<WordBank>(text)
        cached = bank
        return bank
    }
}
