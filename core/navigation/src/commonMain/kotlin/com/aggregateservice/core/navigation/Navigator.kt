package com.aggregateservice.core.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition

/**
 * Главный NavHost для приложения.
 *
 * Использует Voyager для навигации.
 *
 * @param startScreen Стартовый экран (должен быть передан из вызывающего кода)
 */
@Composable
fun AppNavHost(
    startScreen: Screen,
) {
    Navigator(screen = startScreen) { navigator ->
        SlideTransition(navigator) { currentScreen ->
            currentScreen.Content()
        }
    }
}
