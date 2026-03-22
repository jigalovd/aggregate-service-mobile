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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.feature.services.presentation.screenmodel.ServiceFormScreenModel

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
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Service" else "New Service") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancel")
                    }
                },
            )
        },
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
                            text = "Error: ${uiState.error?.message}",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Name field
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text("Service Name *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Description field
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                    )

                    // Price field
                    OutlinedTextField(
                        value = uiState.basePrice,
                        onValueChange = onPriceChange,
                        label = { Text("Base Price *") },
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
                        label = { Text("Duration (minutes) *") },
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
                        label = { Text("Category ID *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Active switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Active")
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

                    Spacer(modifier = Modifier.height(16.dp))

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
                            Text(if (isEditMode) "Update Service" else "Create Service")
                        }
                    }
                }
            }
        }
    }
}
