package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.AuthPromptTrigger
import org.koin.compose.koinInject

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
    i18nProvider: I18nProvider,
    trigger: AuthPromptTrigger,
    onDismiss: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (trigger) {
        AuthPromptTrigger.Booking -> i18nProvider[StringKey.GuestPrompt.BOOKING_TITLE]
        AuthPromptTrigger.Review -> i18nProvider[StringKey.GuestPrompt.REVIEW_TITLE]
        AuthPromptTrigger.Favorites -> i18nProvider[StringKey.GuestPrompt.FAVORITES_TITLE]
    }

    val message = when (trigger) {
        AuthPromptTrigger.Booking -> i18nProvider[StringKey.GuestPrompt.BOOKING_MESSAGE]
        AuthPromptTrigger.Review -> i18nProvider[StringKey.GuestPrompt.REVIEW_MESSAGE]
        AuthPromptTrigger.Favorites -> i18nProvider[StringKey.GuestPrompt.FAVORITES_MESSAGE]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onRegister) {
                Text(i18nProvider[StringKey.GuestPrompt.CREATE_ACCOUNT])
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(i18nProvider[StringKey.GuestPrompt.MAYBE_LATER])
            }
        },
    )
}

/**
 * Preview function for AuthPromptDialog.
 */
@Composable
fun AuthPromptDialogPreview() {
    val i18nProvider: I18nProvider = koinInject()
    AuthPromptDialog(
        i18nProvider = i18nProvider,
        trigger = AuthPromptTrigger.Booking,
        onDismiss = {},
        onRegister = {},
        onLogin = {},
    )
}
