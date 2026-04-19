package com.aggregateservice.feature.provider.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Provider Onboarding Wizard Screen.
 * 
 * 3-step wizard:
 * 1. Basic Info - name, bio, phone
 * 2. Location - address, service area
 * 3. Services - select categories to offer
 */
@Composable
fun ProviderOnboardingScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {},
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Step indicator
        Text("Step ${currentStep + 1} of $totalSteps")

        // Step content
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when (currentStep) {
                0 -> Text("Step 1: Basic Info - TODO")
                1 -> Text("Step 2: Location - TODO")
                2 -> Text("Step 3: Services - TODO")
            }
        }

        // Navigation buttons
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (currentStep > 0) {
                TextButton(onClick = { currentStep-- }) {
                    Text("Back")
                }
            }

            if (currentStep < totalSteps - 1) {
                Button(onClick = { currentStep++ }) {
                    Text("Next")
                }
            } else {
                Button(onClick = onComplete) {
                    Text("Complete")
                }
            }
        }
    }
}
