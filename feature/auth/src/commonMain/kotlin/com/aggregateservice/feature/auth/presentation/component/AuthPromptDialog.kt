package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.AuthPromptTrigger
import com.aggregateservice.feature.auth.domain.usecase.QuickAuthUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Soft registration prompt dialog with Firebase Auth integration.
 *
 * **UX Principles:**
 * - Non-blocking: Easy to dismiss with "Maybe Later"
 * - Clear value proposition: Explains why to register
 * - Minimal friction: One tap to sign in with Google
 *
 * **Architecture:**
 * Uses [QuickAuthUseCase] for the full auth flow (Firebase token + backend verify).
 * No direct dependency on FirebaseAuthApi or AuthRepository.
 *
 * @param trigger What action triggered the prompt
 * @param onDismiss Callback when user dismisses (Maybe Later)
 * @param onAuthSuccess Callback when authentication completes successfully
 * @param modifier Optional modifier
 */
@Composable
fun AuthPromptDialog(
    i18nProvider: I18nProvider,
    trigger: AuthPromptTrigger,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val quickAuthUseCase: QuickAuthUseCase = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = modifier,
        title = { Text(title) },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            } else {
                Text(message)
            }
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            quickAuthUseCase().fold(
                                onSuccess = {
                                    onAuthSuccess()
                                },
                                onFailure = { throwable ->
                                    errorMessage = throwable.message
                                        ?: i18nProvider[StringKey.ERROR]
                                },
                            )
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier,
                ) {
                    Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_GOOGLE])
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(i18nProvider[StringKey.GuestPrompt.MAYBE_LATER])
            }
        },
    )
}
