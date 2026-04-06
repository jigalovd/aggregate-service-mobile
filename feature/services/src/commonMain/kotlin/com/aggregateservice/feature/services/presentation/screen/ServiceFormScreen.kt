package com.aggregateservice.feature.services.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.services.presentation.screenmodel.ServiceFormScreenModel
import org.koin.compose.koinInject

/**
 * Voyager Screen for creating/editing a service.
 *
 * @property serviceId Service ID for edit mode (null for create mode)
 */
data class ServiceFormScreen(
    val serviceId: String? = null,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ServiceFormScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        LaunchedEffect(serviceId) {
            if (serviceId != null) {
                screenModel.loadService(serviceId)
            }
        }

        // Navigate back on successful save
        LaunchedEffect(uiState.saveSuccess) {
            if (uiState.saveSuccess) {
                navigator.pop()
            }
        }

        ServiceFormScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            isEditMode = serviceId != null,
            onNameChange = screenModel::onNameChange,
            onDescriptionChange = screenModel::onDescriptionChange,
            onPriceChange = screenModel::onPriceChange,
            onDurationChange = screenModel::onDurationChange,
            onCategoryChange = screenModel::onCategoryChange,
            onActiveChange = screenModel::onActiveChange,
            onSave = screenModel::saveService,
            onBack = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormScreenContent(
    i18nProvider: I18nProvider,
    uiState: com.aggregateservice.feature.services.presentation.model.ServiceFormUiState,
    isEditMode: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && isEditMode && uiState.name.isEmpty() -> {
                // Error loading existing service
                Box(
                    modifier = Modifier
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
                        TextButton(onClick = onBack) {
                            Text(i18nProvider[StringKey.Services.GO_BACK])
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.MD)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                ) {
                    // Name field
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text(i18nProvider[StringKey.Services.SERVICE_NAME]) },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Description field
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text(i18nProvider[StringKey.Services.DESCRIPTION]) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                    )

                    // Price field
                    OutlinedTextField(
                        value = uiState.basePrice,
                        onValueChange = onPriceChange,
                        label = { Text(i18nProvider[StringKey.Services.BASE_PRICE]) },
                        isError = uiState.priceError != null,
                        supportingText = uiState.priceError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Duration field
                    OutlinedTextField(
                        value = uiState.durationMinutes,
                        onValueChange = onDurationChange,
                        label = { Text(i18nProvider[StringKey.Services.DURATION_MINUTES]) },
                        isError = uiState.durationError != null,
                        supportingText = uiState.durationError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Category field (simplified - in real app would be a dropdown)
                    OutlinedTextField(
                        value = uiState.categoryId,
                        onValueChange = onCategoryChange,
                        label = { Text(i18nProvider[StringKey.Services.CATEGORY]) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Active switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(i18nProvider[StringKey.Services.ACTIVE])
                        Switch(
                            checked = uiState.isActive,
                            onCheckedChange = onActiveChange,
                        )
                    }

                    // Error message
                    uiState.error?.let { error ->
                        Text(
                            text = error.message ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Save button
                    Button(
                        onClick = onSave,
                        enabled = uiState.isValid && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(if (isEditMode) i18nProvider[StringKey.SAVE] else i18nProvider[StringKey.Services.ADD_SERVICE])
                        }
                    }
                }
            }
        }
    }
}
