package com.aggregateservice.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Screen for Google Sign-In.
 *
 * Flow:
 * 1. User taps "Sign in with Google"
 * 2. Firebase Auth handles Google sign-in
 * 3. Firebase token is verified with backend via AuthRepository
 * 4. On success, navigate back to previous screen
 */
class GoogleLoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val i18nProvider: I18nProvider = koinInject()
        val firebaseAuthApi: FirebaseAuthApi = koinInject()
        val authRepository: AuthRepository = koinInject()
        val coroutineScope = rememberCoroutineScope()

        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = i18nProvider[StringKey.Auth.SIGN_IN_TITLE],
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = i18nProvider[StringKey.Auth.SIGN_IN_SUBTITLE],
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null

                                    val firebaseResult = firebaseAuthApi.signInWithGoogle()

                                    firebaseResult.fold(
                                        onSuccess = { firebaseToken ->
                                            val verifyResult = authRepository.verifyFirebaseToken(
                                                authProvider = firebaseToken.authProvider,
                                                firebaseToken = firebaseToken.idToken,
                                            )

                                            verifyResult.fold(
                                                onSuccess = {
                                                    // Auth successful, navigate back
                                                    navigator.pop()
                                                },
                                                onFailure = { error ->
                                                    errorMessage = error.message
                                                        ?: i18nProvider[StringKey.ERROR]
                                                }
                                            )
                                        },
                                        onFailure = { throwable ->
                                            errorMessage = throwable.message
                                                ?: i18nProvider[StringKey.ERROR]
                                        }
                                    )

                                    isLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_GOOGLE])
                        }
                    }

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}