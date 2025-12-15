package com.example.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cryptotracker.presentation.MainScreen
import com.example.cryptotracker.ui.theme.CryptoTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            // Note: We haven't created a Theme.kt yet, so use MaterialTheme or
            // the default theme Android Studio created for you (e.g., CryptoTrackerTheme)
            CryptoTrackerTheme {
                // Just one line! The Activity delegates everything to MainScreen.
                MainScreen()
            }
        }
    }
}
