package com.aggregateservice.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.auth.domain.model.UserRole
import com.aggregateservice.feature.auth.presentation.model.RegistrationUiState
import com.aggregateservice.feature.auth.presentation.screenmodel.RegistrationScreenModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Composable Screen для экрана регистрации (Presentation слой).
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
class RegistrationScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<RegistrationScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current
        val i18nProvider: I18nProvider = koinInject()
        val catalogNavigator: CatalogNavigator = koinInject()

        RegistrationScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onEmailChanged = screenModel::onEmailChanged,
            onPasswordChanged = screenModel::onPasswordChanged,
            onConfirmPasswordChanged = screenModel::onConfirmPasswordChanged,
            onPhoneChanged = screenModel::onPhoneChanged,
            onRoleToggled = screenModel::onRoleToggled,
            onRegisterClick = screenModel::onRegisterClick,
            onClearError = screenModel::clearError,
            onNavigateToLogin = screenModel::onNavigateToLogin,
            onNavigationHandled = screenModel::onNavigationHandled,
            onRegistrationSuccess = {
                // Navigate to main screen after successful registration
                navigator?.replaceAll(catalogNavigator.createCatalogScreen())
            },
            onNavigateToLoginScreen = {
                navigator?.pop()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList", "LongMethod")
@Composable
fun RegistrationScreenContent(
    i18nProvider: I18nProvider,
    uiState: RegistrationUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onRoleToggled: (UserRole) -> Unit,
    onRegisterClick: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigationHandled: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
    LaunchedEffect(uiState.isRegistrationSuccess) {
        if (uiState.isRegistrationSuccess) {
            onRegistrationSuccess()
        }
    }

    // Navigate to login screen
    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onNavigateToLoginScreen()
            onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18nProvider[StringKey.Auth.SIGN_UP]) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.MD)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
            )

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
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } },
            )

            // Confirm password field
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = { Text(i18nProvider[StringKey.Auth.CONFIRM_PASSWORD]) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading,
                singleLine = true,
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
            )

            // Phone field (optional)
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChanged,
                label = { Text(i18nProvider[StringKey.Auth.PHONE]) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                enabled = !uiState.isLoading,
                singleLine = true,
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError?.let { { Text(it) } },
            )

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Role selection
            Text(
                text = i18nProvider[StringKey.Auth.SELECT_ROLE],
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            // Client role checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = UserRole.CLIENT in uiState.selectedRoles,
                    onCheckedChange = { onRoleToggled(UserRole.CLIENT) },
                    enabled = !uiState.isLoading,
                )
                Text(
                    text = i18nProvider[StringKey.Auth.CLIENT_ROLE],
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Provider role checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = UserRole.PROVIDER in uiState.selectedRoles,
                    onCheckedChange = { onRoleToggled(UserRole.PROVIDER) },
                    enabled = !uiState.isLoading,
                )
                Text(
                    text = i18nProvider[StringKey.Auth.PROVIDER_ROLE],
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.MD))

            // Register button
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canRegister(),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(Spacing.LG),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(i18nProvider[StringKey.Auth.SIGN_UP])
                }
            }

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Login link
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading,
            ) {
                Text("${i18nProvider[StringKey.Onboarding.ALREADY_HAVE_ACCOUNT]} ${i18nProvider[StringKey.Onboarding.SIGN_IN]}")
            }
        }
    }
}
