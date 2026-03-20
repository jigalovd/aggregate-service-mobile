package com.aggregateservice.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aggregateservice.core.navigation.AppNavHost
import com.aggregateservice.feature.auth.presentation.screen.LoginScreen

/**
 * Главная Activity для Android приложения.
 *
 * **Architecture:**
 * - Entry point для Android
 * - Использует Compose для UI
 * - Навигация через Voyager (AppNavHost)
 * - Start screen = LoginScreen (пока нет аутентификации)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppNavHost(
                        startScreen = LoginScreen(),
                    )
                }
            }
        }
    }
}
