package com.metehanyl.dilogrenme

import android.app.Application
import com.metehanyl.dilogrenme.speech.TtsManager

class DilOgrenmeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TtsManager.init(this)
    }
}
