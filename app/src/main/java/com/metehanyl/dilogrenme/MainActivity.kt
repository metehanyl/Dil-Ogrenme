package com.metehanyl.dilogrenme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.metehanyl.dilogrenme.ui.DilOgrenmeNavGraph
import com.metehanyl.dilogrenme.ui.theme.DilOgrenmeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DilOgrenmeTheme {
                DilOgrenmeNavGraph()
            }
        }
    }
}
