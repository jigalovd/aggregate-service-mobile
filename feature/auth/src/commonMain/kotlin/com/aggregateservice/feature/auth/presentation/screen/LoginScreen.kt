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
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
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
        val screenModel = koinScreenModel<LoginScreenModel>()
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
            // Platform flags
            showEmailLogin = uiState.showEmailLogin,
            isIOS = uiState.isIOS,
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
    // Platform flags
    showEmailLogin: Boolean = false,
    isIOS: Boolean = false,
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
                .padding(Spacing.MD),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Email field (only shown when email login is enabled)
            if (showEmailLogin) {
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

                Spacer(modifier = Modifier.height(Spacing.SM))

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

                Spacer(modifier = Modifier.height(Spacing.MD))
            }

            // Firebase Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.MD),
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = i18nProvider[StringKey.Auth.OR_DIVIDER],
                    modifier = Modifier.padding(horizontal = Spacing.SM),
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
                        modifier = Modifier.height(Spacing.LG),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_GOOGLE])
                }
            }

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Apple Sign-In Button (iOS only)
            if (isIOS) {
                Button(
                    onClick = onAppleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
                ) {
                    Text(i18nProvider[StringKey.Auth.SIGN_IN_WITH_APPLE])
                }

                Spacer(modifier = Modifier.height(Spacing.SM))
            }

            // Phone Auth Toggle
            TextButton(
                onClick = onPhoneModeToggle,
                enabled = !uiState.isLoading && !uiState.isFirebaseLoading,
            ) {
                Text(
                    if (uiState.phoneAuth.isInPhoneMode) i18nProvider[StringKey.Auth.HIDE_PHONE_AUTH]
                    else i18nProvider[StringKey.Auth.SIGN_IN_WITH_PHONE]
                )
            }

            // Phone Auth Section (inline)
            if (uiState.phoneAuth.isInPhoneMode) {
                Spacer(modifier = Modifier.height(Spacing.SM))

                // Country code + Phone input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                ) {
                    OutlinedTextField(
                        value = uiState.phoneAuth.countryCode,
                        onValueChange = onCountryCodeChanged,
                        label = { Text(i18nProvider[StringKey.Auth.CODE]) },
                        modifier = Modifier.weight(0.25f),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading,
                    )
                    OutlinedTextField(
                        value = uiState.phoneAuth.phoneNumber,
                        onValueChange = onPhoneNumberChanged,
                        label = { Text(i18nProvider[StringKey.Auth.PHONE_NUMBER]) },
                        modifier = Modifier.weight(0.75f),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading && !uiState.phoneAuth.isWaitingForCode,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.SM))

                if (!uiState.phoneAuth.isWaitingForCode) {
                    Button(
                        onClick = onSendPhoneCode,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.phoneAuth.phoneNumber.isNotBlank() && !uiState.isFirebaseLoading,
                    ) {
                        Text(i18nProvider[StringKey.Auth.SEND_CODE])
                    }
                } else {
                    // Verification code input
                    OutlinedTextField(
                        value = localPhoneVerificationCode,
                        onValueChange = {
                            localPhoneVerificationCode = it
                            // Update the state in screen model via a dedicated handler
                        },
                        label = { Text(i18nProvider[StringKey.Auth.VERIFICATION_CODE]) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isFirebaseLoading,
                    )

                    Spacer(modifier = Modifier.height(Spacing.SM))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            onClick = onSendPhoneCode,
                            enabled = uiState.phoneAuth.isResendAvailable && !uiState.isFirebaseLoading,
                        ) {
                            Text(
                                if (uiState.phoneAuth.isResendAvailable) i18nProvider[StringKey.Auth.RESEND_CODE]
                                else "${i18nProvider[StringKey.Auth.RESEND_IN]} ${uiState.phoneAuth.resendCountdown}s"
                            )
                        }

                        Button(
                            onClick = onVerifyPhoneCode,
                            enabled = localPhoneVerificationCode.isNotBlank() && !uiState.isFirebaseLoading,
                        ) {
                            Text(i18nProvider[StringKey.Auth.VERIFY])
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.MD))

            // Login button (only shown when email login is enabled)
            if (showEmailLogin) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canLogin(),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(Spacing.LG),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(i18nProvider[StringKey.Auth.LOGIN])
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.SM))

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
