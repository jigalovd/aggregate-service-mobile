package com.aggregateservice.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.presentation.component.LinkAccountDialog
import com.aggregateservice.feature.auth.presentation.model.LinkAccountState
import com.aggregateservice.feature.auth.presentation.model.LoginUiState
import com.aggregateservice.feature.auth.presentation.screenmodel.LoginScreenModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Composable Screen для экрана логина (Presentation слой).
 *
 * **Architecture:**
 * - UI Layer (Compose) = View в Android architecture
 * - Отображает State
 * - Отправляет Intents (callback'и)
 * - НЕ содержит бизнес-логики
 *
 * **UDF Pattern:**
 * ```
 * UI Events (onClick, onTextChanged)
 *   ↓
 * ScreenModel (обработка)
 *   ↓
 * UseCase (бизнес-логика)
 *   ↓
 * Repository (данные)
 *   ↓
 * StateFlow (новый state)
 *   ↓
 * UI рекомпозиция
 * ```
 */
class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val authRepository: AuthRepository = koinInject()
        val screenModel = koinScreenModel<LoginScreenModel> { parametersOf(authRepository) }
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current
        val i18nProvider: I18nProvider = koinInject()

        LoginScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onEmailChanged = screenModel::onEmailChanged,
            onPasswordChanged = screenModel::onPasswordChanged,
            onLoginClick = screenModel::onLoginClick,
            onClearError = screenModel::clearError,
            onLoginSuccess = {
                // Navigate to main screen
                // navigator.push(MainScreen())
            },
            // Firebase handlers
            onGoogleSignIn = screenModel::onGoogleSignIn,
            onAppleSignIn = screenModel::onAppleSignIn,
            onPhoneModeToggle = screenModel::onPhoneModeToggle,
            onPhoneNumberChanged = screenModel::onPhoneNumberChanged,
            onCountryCodeChanged = screenModel::onCountryCodeChanged,
            onSendPhoneCode = screenModel::onSendPhoneCode,
            onVerifyPhoneCode = screenModel::onVerifyPhoneCode,
            // Link account
            linkAccountState = uiState.linkAccount,
            onLinkAccount = screenModel::onLinkAccount,
            onDismissLinkDialog = screenModel::onDismissLinkDialog,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    i18nProvider: I18nProvider,
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onClearError: () -> Unit,
    onLoginSuccess: () -> Unit,
    // Firebase handlers
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    onPhoneModeToggle: () -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onCountryCodeChanged: (String) -> Unit,
    onSendPhoneCode: () -> Unit,
    onVerifyPhoneCode: () -> Unit,
    // Link account
    linkAccountState: LinkAccountState,
    onLinkAccount: (String) -> Unit,
    onDismissLinkDialog: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var localPhoneVerificationCode by remember { mutableStateOf("") }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearError()
            }
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18nProvider[StringKey.Auth.LOGIN]) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Email field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text(i18nProvider[StringKey.Auth.EMAIL]) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
                enabled = !uiState.isLoading,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = { Text(i18nProvider[StringKey.Auth.PASSWORD]) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Firebase Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = " or ",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Google Sign-In Button
            Button(
                onClick = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
            ) {
                if (uiState.isFirebaseLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Sign in with Google")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Apple Sign-In Button
            Button(
                onClick = onAppleSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
            ) {
                Text("Sign in with Apple")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Auth Toggle
            TextButton(
                onClick = onPhoneModeToggle,
                enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
            ) {
                Text(
                    if (uiState.phoneAuth.isInPhoneMode) "Hide phone auth"
                    else "Sign in with phone"
                )
            }

            // Phone Auth Section (inline)
            if (uiState.phoneAuth.isInPhoneMode) {
                Spacer(modifier = Modifier.height(8.dp))

                // Country code + Phone input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.phoneAuth.countryCode,
                        onValueChange = onCountryCodeChanged,
                        label = { Text("Code") },
                        modifier = Modifier.weight(0.25f),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading,
                    )
                    OutlinedTextField(
                        value = uiState.phoneAuth.phoneNumber,
                        onValueChange = onPhoneNumberChanged,
                        label = { Text("Phone") },
                        modifier = Modifier.weight(0.75f),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading && !uiState.phoneAuth.isWaitingForCode,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!uiState.phoneAuth.isWaitingForCode) {
                    Button(
                        onClick = onSendPhoneCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.phoneAuth.phoneNumber.isNotBlank() && !uiState.isFirebaseLoading,
                    ) {
                        Text("Send Code")
                    }
                } else {
                    // Verification code input
                    OutlinedTextField(
                        value = localPhoneVerificationCode,
                        onValueChange = {
                            localPhoneVerificationCode = it
                            // Update the state in screen model via a dedicated handler
                        },
                        label = { Text("Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            onClick = onSendPhoneCode,
                            enabled = uiState.phoneAuth.isResendAvailable && !uiState.isFirebaseLoading,
                        ) {
                            Text(
                                if (uiState.phoneAuth.isResendAvailable) "Resend Code"
                                else "Resend in ${uiState.phoneAuth.resendCountdown}s"
                            )
                        }

                        Button(
                            onClick = onVerifyPhoneCode,
                            enabled = localPhoneVerificationCode.isNotBlank() && !uiState.isFirebaseLoading,
                        ) {
                            Text("Verify")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canLogin(),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(i18nProvider[StringKey.Auth.LOGIN])
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot password button
            // Feature: Forgot password navigation planned for v1.1
            TextButton(
                onClick = { /* No-op: Forgot password feature pending */ },
                enabled = !uiState.isLoading,
            ) {
                Text(i18nProvider[StringKey.Auth.FORGOT_PASSWORD])
            }
        }
    }

    // Link Account Dialog
    if (linkAccountState.showDialog) {
        LinkAccountDialog(
            i18nProvider = i18nProvider,
            email = linkAccountState.email,
            authProvider = linkAccountState.authProvider,
            onLink = onLinkAccount,
            onDismiss = onDismissLinkDialog,
        )
    }
}
