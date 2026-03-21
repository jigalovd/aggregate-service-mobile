package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aggregateservice.core.navigation.AuthPromptTrigger

/**
 * Soft registration prompt dialog.
 *
 * **UX Principles:**
 * - Non-blocking: Easy to dismiss with "Maybe Later"
 * - Clear value proposition: Explains why to register
 * - Minimal friction: One tap to dismiss
 * - Dual action: Register or Sign In options
 *
 * **Usage:**
 * ```kotlin
 * if (showAuthPrompt) {
 *     AuthPromptDialog(
 *         trigger = AuthPromptTrigger.Booking,
 *         onDismiss = { showAuthPrompt = false },
 *         onRegister = { navigator.push(RegisterScreen()) },
 *         onLogin = { navigator.push(LoginScreen()) },
 *     )
 * }
 * ```
 *
 * @param trigger What action triggered the prompt
 * @param onDismiss Callback when user dismisses (Maybe Later)
 * @param onRegister Callback when user chooses to create account
 * @param onLogin Callback when user chooses to sign in
 * @param modifier Optional modifier
 */
@Composable
fun AuthPromptDialog(
    trigger: AuthPromptTrigger,
    onDismiss: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (trigger) {
        AuthPromptTrigger.Booking -> "Book this service?"
        AuthPromptTrigger.Review -> "Share your experience?"
        AuthPromptTrigger.Favorites -> "Save for later?"
    }

    val message = when (trigger) {
        AuthPromptTrigger.Booking -> "Create an account to book appointments and manage your schedule."
        AuthPromptTrigger.Review -> "Register to leave reviews and help others find great services."
        AuthPromptTrigger.Favorites -> "Sign in to save your favorite providers and access them anytime."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onRegister) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        },
    )
}

/**
 * Preview function for AuthPromptDialog.
 */
@Composable
fun AuthPromptDialogPreview() {
    AuthPromptDialog(
        trigger = AuthPromptTrigger.Booking,
        onDismiss = {},
        onRegister = {},
        onLogin = {},
    )
}
