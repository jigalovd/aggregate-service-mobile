package com.aggregateservice.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.core.navigation.ProviderNavigator
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.profile.domain.model.Profile
import com.aggregateservice.feature.profile.presentation.screenmodel.ProfileScreenModel
import org.koin.compose.koinInject

import com.aggregateservice.core.navigation.RequiredRole

/**
 * Voyager Screen for user profile management.
 */
object ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProfileScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()
        val authNavigator: AuthNavigator = koinInject()
        val authStateProvider: AuthStateProvider = koinInject()
        val providerNavigator: ProviderNavigator = koinInject()
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow

        // Observe auth state reactively
        val authState by authStateProvider.authState.collectAsState()

        // Navigate to login screen when not authenticated
        // Use push (not replaceAll) so LoginScreen can pop back here after login
        LaunchedEffect(authState) {
            if (authState !is AuthState.Authenticated) {
                navigator.push(authNavigator.createLoginScreen())
            }
        }

        // Load profile only when authenticated
        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated) {
                screenModel.loadProfile()
            }
        }

        // Show success snackbar
        LaunchedEffect(uiState.saveSuccess) {
            if (uiState.saveSuccess) {
                snackbarHostState.showSnackbar(
                    message = i18nProvider[StringKey.SUCCESS],
                    duration = SnackbarDuration.Short,
                )
                screenModel.clearSaveSuccess()
            }
        }

        ProfileScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onStartEditing = screenModel::startEditing,
            onCancelEditing = screenModel::cancelEditing,
            onFullNameChanged = screenModel::onFullNameChanged,
            onPhoneChanged = screenModel::onPhoneChanged,
            onSave = screenModel::saveProfile,
            onRetry = { screenModel.loadProfile() },
            onErrorDismiss = screenModel::clearError,
            onLogout = {
                screenModel.logout(navigator)
            },
            onSwitchRole = { newRole ->
                val currentState = authState
                if (currentState is AuthState.Authenticated) {
                    screenModel.switchRole(currentState.roles.toList(), newRole)
                }
            },
            onSwitchToProvider = {
                val currentState = authState
                if (currentState is AuthState.Authenticated) {
                    screenModel.switchRole(currentState.roles.toList(), "PROVIDER")
                    providerNavigator.navigateToDashboard(navigator)
                }
            },
            currentRole = (authState as? AuthState.Authenticated)?.currentRole,
            availableRoles = (authState as? AuthState.Authenticated)?.roles?.toList() ?: emptyList(),
            navigator = navigator,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    i18nProvider: I18nProvider,
    uiState: com.aggregateservice.feature.profile.presentation.model.ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onErrorDismiss: () -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: (String) -> Unit,
    onSwitchToProvider: () -> Unit,
    currentRole: String?,
    availableRoles: List<String>,
    navigator: cafe.adriel.voyager.navigator.Navigator,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${i18nProvider[StringKey.ERROR]}: ${uiState.error?.message}",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.MD))
                        TextButton(onClick = onRetry) {
                            Text(i18nProvider[StringKey.RETRY])
                        }
                    }
                }
            }

            uiState.hasProfile -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                ) {
                    // Avatar and basic info
                    ProfileHeader(profile = uiState.profile!!)

                    Spacer(modifier = Modifier.height(Spacing.LG))

                    if (uiState.isEditing) {
                        EditProfileForm(
                            uiState = uiState,
                            onFullNameChanged = onFullNameChanged,
                            onPhoneChanged = onPhoneChanged,
                            onSave = onSave,
                            onCancel = onCancelEditing,
                            i18nProvider = i18nProvider,
                        )
                    } else {
                        ViewProfileInfo(
                            profile = uiState.profile,
                            currentRole = currentRole,
                            availableRoles = availableRoles,
                            onEdit = onStartEditing,
                            onLogout = onLogout,
                            onSwitchRole = onSwitchRole,
                            onSwitchToProvider = onSwitchToProvider,
                            i18nProvider = i18nProvider,
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.LG))

                    // Stats section
                    ProfileStats(profile = uiState.profile)
                }
            }
        }

        // Error dialog
        if (uiState.error != null && !uiState.isLoading) {
            AlertDialog(
                onDismissRequest = onErrorDismiss,
                title = { Text(i18nProvider[StringKey.ERROR]) },
                text = { Text(uiState.error?.message ?: "Unknown error") },
                confirmButton = {
                    TextButton(onClick = onErrorDismiss) {
                        Text(i18nProvider[StringKey.OK])
                    }
                },
            )
        }
    }
}

@Composable
fun ProfileHeader(profile: Profile) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        AsyncImage(
            model = profile.avatarUrl,
            contentDescription = "Profile avatar",
            modifier =
                Modifier
                    .size(100.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(Spacing.MD))

        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
fun ViewProfileInfo(
    profile: Profile,
    currentRole: String?,
    availableRoles: List<String>,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: (String) -> Unit,
    onSwitchToProvider: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MD),
    ) {
        ProfileInfoRow(
            label = i18nProvider[StringKey.Profile.FULL_NAME],
            value = profile.fullName ?: i18nProvider[StringKey.Profile.NOT_SET],
        )

        Spacer(modifier = Modifier.height(Spacing.XXS))

        ProfileInfoRow(
            label = i18nProvider[StringKey.Profile.PHONE],
            value = profile.phone ?: i18nProvider[StringKey.Profile.NOT_SET],
        )

        // Current role indicator
        if (currentRole != null) {
            Spacer(modifier = Modifier.height(Spacing.MD))
            ProfileInfoRow(
                label = "Role",
                value = currentRole.replaceFirstChar { it.uppercase() },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.LG))

        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(i18nProvider[StringKey.Profile.EDIT])
        }

        // Role switch button (if user has multiple roles)
        if (availableRoles.size > 1) {
            Spacer(modifier = Modifier.height(Spacing.SM))
            val otherRole = availableRoles.firstOrNull { it != currentRole }
            if (otherRole != null) {
                Button(
                    onClick = { onSwitchRole(otherRole) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Switch to ${otherRole.replaceFirstChar { it.uppercase() }}")
                }
            }
        }

        // Switch to Provider button (visible when user has PROVIDER role and is not currently provider)
        if (availableRoles.contains("PROVIDER") && currentRole != "PROVIDER") {
            Spacer(modifier = Modifier.height(Spacing.SM))
            OutlinedButton(
                onClick = onSwitchToProvider,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Switch to Provider")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.SM))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(i18nProvider[StringKey.Profile.LOGOUT])
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun EditProfileForm(
    uiState: com.aggregateservice.feature.profile.presentation.model.ProfileUiState,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MD),
    ) {
        OutlinedTextField(
            value = uiState.editFullName,
            onValueChange = onFullNameChanged,
            label = { Text(i18nProvider[StringKey.Profile.FULL_NAME]) },
            isError = uiState.fullNameError != null,
            supportingText = uiState.fullNameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.MD))

        OutlinedTextField(
            value = uiState.editPhone,
            onValueChange = onPhoneChanged,
            label = { Text(i18nProvider[StringKey.Profile.PHONE]) },
            isError = uiState.phoneError != null,
            supportingText = uiState.phoneError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.LG))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving,
            ) {
                Text(i18nProvider[StringKey.CANCEL])
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && uiState.isFormValid,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = Spacing.XXS,
                    )
                } else {
                    Text(i18nProvider[StringKey.SAVE])
                }
            }
        }
    }
}

@Composable
fun ProfileStats(profile: Profile) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MD),
    ) {
        Text(
            text = "Booking Statistics",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(Spacing.MD))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                label = "No-shows",
                value = profile.noShowCount.toString(),
            )

            StatItem(
                label = "No-show Rate",
                value = "${(profile.noShowRate * 100).toInt()}%",
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
