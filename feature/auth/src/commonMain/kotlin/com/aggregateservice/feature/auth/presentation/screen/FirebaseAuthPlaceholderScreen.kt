package com.aggregateservice.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen

/**
 * Placeholder screen when auth is needed.
 *
 * NOTE: Firebase Auth handles authentication via its own UI.
 * This screen should not be shown in normal flow - instead,
 * Firebase Auth should be triggered directly via AuthPromptDialog.
 */
class FirebaseAuthPlaceholderScreen : Screen {
    @Composable
    override fun Content() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Please sign in with Google or Apple")
        }
    }
}
