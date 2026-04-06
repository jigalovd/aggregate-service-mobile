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
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.presentation.screenmodel.SelectServiceScreenModel
import org.koin.compose.koinInject

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
        val i18nProvider: I18nProvider = koinInject()

        // Load services on first composition
        LaunchedEffect(providerId) {
            screenModel.loadServices(providerId)
        }

        SelectServiceScreenContent(
            i18nProvider = i18nProvider,
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
    i18nProvider: I18nProvider,
    providerName: String,
    uiState: com.aggregateservice.feature.booking.presentation.model.SelectServiceUiState,
    onServiceToggle: (BookingService) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (uiState.hasSelection) {
                ServiceSelectionBottomBar(
                    totalPrice = uiState.formattedTotal,
                    totalDuration = uiState.formattedDuration,
                    servicesCount = uiState.selectedServices.size,
                    onContinue = onContinue,
                    i18nProvider = i18nProvider,
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
                        text = "${i18nProvider[StringKey.ERROR]}: ${uiState.error?.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            uiState.nonCombinableError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.nonCombinableError ?: "",
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
                    Text(i18nProvider[StringKey.Booking.NO_SERVICES])
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
            .padding(horizontal = Spacing.MD, vertical = Spacing.SM)
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.MD),
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
                Spacer(modifier = Modifier.height(Spacing.XS))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
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
    i18nProvider: I18nProvider,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.MD),
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
                Text(i18nProvider[StringKey.Booking.CONTINUE])
            }
        }
    }
}
