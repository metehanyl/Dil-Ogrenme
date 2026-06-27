package com.metehanyl.dilogrenme.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** App-wide singleton wrapping Android's TextToSpeech so any screen can request pronunciation
 *  playback without re-initializing the engine. */
object TtsManager {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val pendingQueue = mutableListOf<Pair<String, String>>()

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                pendingQueue.forEach { (text, locale) -> speakNow(text, locale) }
                pendingQueue.clear()
            }
        }
    }

    fun speak(text: String, localeTag: String) {
        if (!ready || tts == null) {
            pendingQueue.add(text to localeTag)
            return
        }
        speakNow(text, localeTag)
    }

    private fun speakNow(text: String, localeTag: String) {
        val engine = tts ?: return
        engine.setLanguage(Locale.forLanguageTag(localeTag))
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dil_ogrenme_${text.hashCode()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
