package com.aggregateservice.feature.services.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.presentation.screenmodel.ServicesListScreenModel
import org.koin.compose.koinInject

/**
 * Voyager Screen for services list management.
 */
class ServicesListScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ServicesListScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        LaunchedEffect(Unit) {
            screenModel.loadServices()
        }

        ServicesListScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onAddService = {
                navigator.push(ServiceFormScreen())
            },
            onEditService = { service ->
                navigator.push(ServiceFormScreen(serviceId = service.id))
            },
            onDeleteConfirm = screenModel::confirmDelete,
            onDeleteConfirmDialog = screenModel::deleteService,
            onDeleteDismiss = screenModel::dismissDeleteDialog,
            onRetry = screenModel::loadServices,
            onErrorDismiss = screenModel::clearError,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesListScreenContent(
    i18nProvider: I18nProvider,
    uiState: com.aggregateservice.feature.services.presentation.model.ServicesListUiState,
    onAddService: () -> Unit,
    onEditService: (ProviderService) -> Unit,
    onDeleteConfirm: (ProviderService) -> Unit,
    onDeleteConfirmDialog: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onRetry: () -> Unit,
    onErrorDismiss: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18nProvider[StringKey.Services.MY_SERVICES]) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddService) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
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

            uiState.error != null -> {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onRetry) {
                            Text(i18nProvider[StringKey.RETRY])
                        }
                    }
                }
            }

            !uiState.hasServices -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(i18nProvider[StringKey.Services.NO_SERVICES])
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onAddService) {
                            Text(i18nProvider[StringKey.Services.ADD_FIRST])
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceListItem(
                            service = service,
                            onEdit = { onEditService(service) },
                            onDelete = { onDeleteConfirm(service) },
                            i18nProvider = i18nProvider,
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = onDeleteDismiss,
                title = { Text(i18nProvider[StringKey.Services.DELETE_SERVICE]) },
                text = { Text(i18nProvider[StringKey.Services.DELETE_CONFIRM].format(uiState.serviceToDelete?.name ?: "")) },
                confirmButton = {
                    TextButton(
                        onClick = onDeleteConfirmDialog,
                        enabled = !uiState.isDeleting,
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp))
                        } else {
                            Text(i18nProvider[StringKey.DELETE], color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDeleteDismiss) {
                        Text(i18nProvider[StringKey.CANCEL])
                    }
                },
            )
        }
    }
}

@Composable
fun ServiceListItem(
    service: ProviderService,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (service.isActive) Color.Unspecified else MaterialTheme.colorScheme.outline,
                )
                service.description?.let { desc ->
                    Text(
                        text = desc.take(80) + if (desc.length > 80) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = service.formattedPrice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = service.formattedDuration,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Switch(
                checked = service.isActive,
                onCheckedChange = null, // Read-only, change in edit screen
            )

            TextButton(onClick = onEdit) {
                Text(i18nProvider[StringKey.Services.EDIT])
            }

            TextButton(onClick = onDelete) {
                Text(i18nProvider[StringKey.DELETE], color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
