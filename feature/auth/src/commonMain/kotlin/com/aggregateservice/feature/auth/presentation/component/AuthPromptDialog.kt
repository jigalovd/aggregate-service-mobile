package com.aggregateservice.feature.auth.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.firebase.FirebaseToken
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.AuthPromptTrigger
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Soft registration prompt dialog with Firebase Auth integration.
 *
 * **UX Principles:**
 * - Non-blocking: Easy to dismiss with "Maybe Later"
 * - Clear value proposition: Explains why to register
 * - Minimal friction: One tap to sign in with Google
 * - Firebase Auth handles the actual authentication
 *
 * **Usage:**
 * ```kotlin
 * if (showAuthPrompt) {
 *     AuthPromptDialog(
 *         trigger = AuthPromptTrigger.Booking,
 *         onDismiss = { showAuthPrompt = false },
 *         onAuthSuccess = { token ->
 *             // Send token to backend, then refresh auth state
 *             showAuthPrompt = false
 *         },
 *     )
 * }
 * ```
 *
 * @param trigger What action triggered the prompt
 * @param onDismiss Callback when user dismisses (Maybe Later)
 * @param onAuthSuccess Callback when Firebase Auth succeeds with FirebaseToken
 * @param modifier Optional modifier
 */
@Composable
fun AuthPromptDialog(
    i18nProvider: I18nProvider,
    trigger: AuthPromptTrigger,
    onDismiss: () -> Unit,
    onAuthSuccess: (FirebaseToken) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firebaseAuthApi: FirebaseAuthApi = koinInject()
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

    // Clear error when dialog is dismissed
    LaunchedEffect(onDismiss) {
        // Error will be cleared when the dialog is dismissed
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = modifier,
        title = { Text(title) },
        text = {
            if (isLoading) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier,
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            } else {
                Text(message)
            }
            errorMessage?.let { error ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier,
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
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
                            val result = firebaseAuthApi.signInWithGoogle()
                            isLoading = false
                            result.fold(
                                onSuccess = { token ->
                                    onAuthSuccess(token)
                                },
                                onFailure = { throwable ->
                                    errorMessage = throwable.message
                                        ?: i18nProvider[StringKey.ERROR]
                                }
                            )
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
        onAuthSuccess = {},
    )
}
