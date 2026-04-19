package com.aggregateservice.feature.provider.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Provider Dashboard Screen.
 * 
 * Displays provider-specific overview: active bookings, earnings summary, upcoming appointments.
 * Requires PROVIDER role to access.
 */
@Composable
fun ProviderDashboardScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Provider Dashboard - TODO: Full implementation")
    }
}
