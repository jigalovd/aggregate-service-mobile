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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.core.firebase.PlatformAuthContext
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.network.toUserMessage
import com.aggregateservice.core.i18n.StringKey
import co.touchlab.kermit.Logger
import com.aggregateservice.feature.auth.presentation.model.LoginScreenModel
import org.koin.compose.koinInject

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val i18nProvider: I18nProvider = koinInject()
        val signInUseCase: SignInUseCase = koinInject()
        val authStateProvider: AuthStateProvider = koinInject()
        val authProviderApi: AuthProviderApi = koinInject()
        val logger = Logger.withTag("Auth")
        val screenModel =
            rememberScreenModel {
                LoginScreenModel(signInUseCase, authStateProvider, authProviderApi, logger)
            }
        val uiState by screenModel.uiState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(uiState.isLoginSuccess) {
            if (uiState.isLoginSuccess) {
                navigator.pop()
            }
        }

        Scaffold { paddingValues ->
            Box(
                modifier =
                    Modifier
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
                        text = i18nProvider[StringKey.Auth.SIGN_IN_TITLE],
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = i18nProvider[StringKey.Auth.SIGN_IN_SUBTITLE],
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                val platformContext = context as? PlatformAuthContext
                                if (platformContext != null) {
                                    screenModel.signIn(platformContext)
                                } else {
                                    screenModel.setError(
                                        "Unable to obtain platform context for sign-in",
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                        ) {
                            Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_GOOGLE])
                        }
                    }

                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error.toUserMessage(i18nProvider),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
