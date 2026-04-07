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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.auth.domain.usecase.SignInWithFirebaseUseCase
import com.aggregateservice.feature.auth.presentation.model.GoogleLoginScreenModel
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
        val signInUseCase: SignInWithFirebaseUseCase = koinInject()
        val i18n: I18nProvider = koinInject()
        val screenModel = rememberScreenModel { GoogleLoginScreenModel(signInUseCase, i18n) }
        val uiState by screenModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        // Navigate back on successful login
        if (uiState.isLoginSuccess) {
            navigator.pop()
        }

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

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    screenModel.signIn()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_GOOGLE])
                        }
                    }

                    uiState.errorMessage?.let { error ->
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
