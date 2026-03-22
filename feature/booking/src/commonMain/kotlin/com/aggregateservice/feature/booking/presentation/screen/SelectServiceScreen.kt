package com.aggregateservice.feature.booking.presentation.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.presentation.screenmodel.SelectServiceScreenModel

/**
 * Voyager Screen для выбора услуг.
 *
 * @property providerId ID мастера
 * @property providerName Название бизнеса мастера
 */
data class SelectServiceScreen(
    val providerId: String,
    val providerName: String,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SelectServiceScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        // Load services on first composition
        LaunchedEffect(providerId) {
            screenModel.loadServices(providerId)
        }

        SelectServiceScreenContent(
            providerName = providerName,
            uiState = uiState,
            onServiceToggle = screenModel::toggleServiceSelection,
            onContinue = {
                val selectedIds = uiState.selectedServices.map { it.id }
                navigator.push(
                    SelectDateTimeScreen(
                        providerId = providerId,
                        providerName = providerName,
                        serviceIds = selectedIds,
                    )
                )
            },
            onBack = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectServiceScreenContent(
    providerName: String,
    uiState: com.aggregateservice.feature.booking.presentation.model.SelectServiceUiState,
    onServiceToggle: (BookingService) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Services - $providerName") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
            )
        },
        bottomBar = {
            if (uiState.hasSelection) {
                ServiceSelectionBottomBar(
                    totalPrice = uiState.formattedTotal,
                    totalDuration = uiState.formattedDuration,
                    servicesCount = uiState.selectedServices.size,
                    onContinue = onContinue,
                )
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
                    Text(
                        text = "Error: ${uiState.error?.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            uiState.services.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No services available")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceSelectionItem(
                            service = service,
                            isSelected = uiState.isSelected(service),
                            onToggle = { onServiceToggle(service) },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceSelectionItem(
    service: BookingService,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                service.description?.let { desc ->
                    Text(
                        text = desc.take(100),
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
        }
    }
}

@Composable
fun ServiceSelectionBottomBar(
    totalPrice: String,
    totalDuration: String,
    servicesCount: Int,
    onContinue: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$servicesCount service${if (servicesCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "$totalPrice • $totalDuration",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Button(onClick = onContinue) {
                Text("Continue")
            }
        }
    }
}
