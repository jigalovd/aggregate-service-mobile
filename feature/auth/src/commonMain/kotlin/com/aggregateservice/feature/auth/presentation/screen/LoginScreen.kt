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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.feature.auth.presentation.model.LoginScreenModel
import org.koin.compose.koinInject

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val signInUseCase: SignInUseCase = koinInject()
        val authStateProvider: AuthStateProvider = koinInject()
        val authProviderApi: AuthProviderApi = koinInject()
        val screenModel = rememberScreenModel {
            LoginScreenModel(signInUseCase, authStateProvider, authProviderApi)
        }
        val uiState by screenModel.uiState.collectAsState()

        LaunchedEffect(uiState.isLoginSuccess) {
            if (uiState.isLoginSuccess) {
                navigator.pop()
            }
        }

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Book beauty and fitness services near you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                // TODO: Pass PlatformAuthContext from platform entry point.
                                // On Android, this should be the Activity context.
                                // On iOS, a different approach is needed.
                                // For now, this requires platform-specific wiring.
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                        ) {
                            Text("Sign in with Google")
                        }
                    }

                    uiState.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
