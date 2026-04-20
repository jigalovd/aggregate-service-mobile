package com.aggregateservice.feature.provider.onboarding.presentation.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aggregateservice.feature.provider.onboarding.OnboardingState
import com.aggregateservice.feature.provider.onboarding.presentation.model.OnboardingUiState
import com.aggregateservice.feature.provider.onboarding.presentation.screenmodel.ProviderOnboardingScreenModel
import org.koin.compose.koinInject

/**
 * Provider Onboarding Wizard Screen.
 *
 * 3-step wizard:
 * 1. Basic Info - business name, bio, phone
 * 2. Location - address, service radius
 * 3. Services - select categories
 *
 * @param modifier Compose modifier
 * @param onComplete Callback when onboarding completes successfully
 */
@Composable
fun ProviderOnboardingScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {},
) {
    val screenModel: ProviderOnboardingScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()

    // Set up completion callback
    LaunchedEffect(Unit) {
        screenModel.onComplete = onComplete
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is OnboardingUiState.Loading -> {
                LoadingContent()
            }

            is OnboardingUiState.Content -> {
                if (state.isSubmitted) {
                    SubmittedContent(onComplete = onComplete)
                } else {
                    OnboardingContent(
                        step = state.step,
                        basicInfo = screenModel.getBasicInfo(),
                        location = screenModel.getLocation(),
                        services = screenModel.getServices(),
                        isValid = state.isValid,
                        validationErrors = state.validationErrors,
                        isSubmitting = state.isSubmitting,
                        onBasicInfoUpdate = { name, bio, phone ->
                            screenModel.updateBasicInfo(
                                businessName = name,
                                bio = bio,
                                phone = phone,
                            )
                        },
                        onLocationUpdate = { address, radius ->
                            screenModel.updateLocation(
                                address = address,
                                serviceRadiusKm = radius,
                            )
                        },
                        onCategoryToggle = { categoryId ->
                            screenModel.toggleCategory(categoryId)
                        },
                        onNext = { screenModel.nextStep() },
                        onPrevious = { screenModel.previousStep() },
                    )
                }
            }

            is OnboardingUiState.Error -> {
                ErrorContent(
                    errorMessage = state.error.message ?: "An error occurred",
                    onRetry = { screenModel.retry() },
                    onBack = { screenModel.previousStep() },
                )
            }
        }
    }
}

/**
 * Loading content while submitting.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Submitting your information...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Successfully submitted content.
 */
@Composable
private fun SubmittedContent(onComplete: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Onboarding Complete!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your provider profile has been created.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onComplete) {
                Text("Go to Dashboard")
            }
        }
    }
}

/**
 * Main onboarding wizard content.
 */
@Composable
private fun OnboardingContent(
    step: Int,
    basicInfo: OnboardingState.BasicInfoStep,
    location: OnboardingState.LocationStep,
    services: OnboardingState.ServicesStep,
    isValid: Boolean,
    validationErrors: Map<String, String>,
    isSubmitting: Boolean,
    onBasicInfoUpdate: (String?, String?, String?) -> Unit,
    onLocationUpdate: (String?, Float?) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Step indicator
        StepIndicator(currentStep = step, totalSteps = OnboardingState.TOTAL_STEPS)

        Spacer(modifier = Modifier.height(24.dp))

        // Step title and description
        StepHeader(step = step)

        Spacer(modifier = Modifier.height(24.dp))

        // Step content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (step) {
                0 -> BasicInfoStep(
                    businessName = basicInfo.businessName,
                    bio = basicInfo.bio,
                    phone = basicInfo.phone,
                    validationErrors = validationErrors,
                    onUpdate = onBasicInfoUpdate,
                )

                1 -> LocationStep(
                    address = location.address,
                    serviceRadiusKm = location.serviceRadiusKm,
                    validationErrors = validationErrors,
                    onUpdate = onLocationUpdate,
                )

                2 -> ServicesStep(
                    selectedCategoryIds = services.selectedCategoryIds,
                    validationErrors = validationErrors,
                    onCategoryToggle = onCategoryToggle,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons
        NavigationButtons(
            step = step,
            isValid = isValid,
            isSubmitting = isSubmitting,
            onNext = onNext,
            onPrevious = onPrevious,
            totalSteps = OnboardingState.TOTAL_STEPS,
        )
    }
}

/**
 * Step progress indicator.
 */
@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Step header with title and description.
 */
@Composable
private fun StepHeader(step: Int) {
    val (title, description) = when (step) {
        0 -> "Basic Information" to "Tell us about your business"
        1 -> "Service Location" to "Where will you provide services?"
        2 -> "Select Services" to "Choose categories you offer"
        else -> "Onboarding" to ""
    }

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Basic info step form.
 */
@Composable
private fun BasicInfoStep(
    businessName: String,
    bio: String,
    phone: String,
    validationErrors: Map<String, String>,
    onUpdate: (String?, String?, String?) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        OutlinedTextField(
            value = businessName,
            onValueChange = { onUpdate(it, null, null) },
            label = { Text("Business Name *") },
            isError = validationErrors.containsKey("businessName"),
            supportingText = validationErrors["businessName"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { onUpdate(null, it.take(OnboardingState.BasicInfoStep.MAX_BIO_LENGTH), null) },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${bio.length}/${OnboardingState.BasicInfoStep.MAX_BIO_LENGTH} characters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { onUpdate(null, null, it.filter { c -> c.isDigit() }) },
            label = { Text("Phone Number *") },
            isError = validationErrors.containsKey("phone"),
            supportingText = validationErrors["phone"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
    }
}

/**
 * Location step form.
 */
@Composable
private fun LocationStep(
    address: String,
    serviceRadiusKm: Float,
    validationErrors: Map<String, String>,
    onUpdate: (String?, Float?) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        OutlinedTextField(
            value = address,
            onValueChange = { onUpdate(it, null) },
            label = { Text("Service Address *") },
            isError = validationErrors.containsKey("address"),
            supportingText = validationErrors["address"]?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Service Radius: ${serviceRadiusKm.toInt()} km",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = serviceRadiusKm,
            onValueChange = { onUpdate(null, it.coerceIn(OnboardingState.LocationStep.MIN_RADIUS_KM, OnboardingState.LocationStep.MAX_RADIUS_KM)) },
            valueRange = OnboardingState.LocationStep.MIN_RADIUS_KM..OnboardingState.LocationStep.MAX_RADIUS_KM,
            steps = ((OnboardingState.LocationStep.MAX_RADIUS_KM - OnboardingState.LocationStep.MIN_RADIUS_KM) / 5).toInt() - 1,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${OnboardingState.LocationStep.MIN_RADIUS_KM.toInt()} km",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "${OnboardingState.LocationStep.MAX_RADIUS_KM.toInt()} km",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (validationErrors.containsKey("serviceRadius")) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationErrors["serviceRadius"] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Services step form with category chips.
 */
@Composable
private fun ServicesStep(
    selectedCategoryIds: Set<String>,
    validationErrors: Map<String, String>,
    onCategoryToggle: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    // Sample categories (in production, load from API)
    val availableCategories = remember {
        listOf(
            "cleaning" to "Home Cleaning",
            "plumbing" to "Plumbing",
            "electrical" to "Electrical",
            "landscaping" to "Landscaping",
            "painting" to "Painting",
            "carpentry" to "Carpentry",
            "hvac" to "HVAC",
            "moving" to "Moving & Delivery",
            "pets" to "Pet Care",
            "tutoring" to "Tutoring",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Text(
            text = "Select the services you offer:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category chips
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            availableCategories.chunked(2).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowCategories.forEach { (id, name) ->
                        CategoryChip(
                            categoryId = id,
                            categoryName = name,
                            isSelected = id in selectedCategoryIds,
                            onClick = { onCategoryToggle(id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining space if odd number
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${selectedCategoryIds.size} category selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (validationErrors.containsKey("categories")) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationErrors["categories"] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Category selection chip.
 */
@Composable
private fun CategoryChip(
    categoryId: String,
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(categoryName)
        }
    } else {
        TextButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(categoryName)
        }
    }
}

/**
 * Navigation buttons.
 */
@Composable
private fun NavigationButtons(
    step: Int,
    isValid: Boolean,
    isSubmitting: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    totalSteps: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Back button
        if (step > 0) {
            TextButton(onClick = onPrevious) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        // Next/Complete button
        if (step < totalSteps - 1) {
            Button(
                onClick = onNext,
                enabled = isValid && !isSubmitting,
            ) {
                Text("Next")
            }
        } else {
            Button(
                onClick = onNext,
                enabled = isValid && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Complete")
                }
            }
        }
    }
}

/**
 * Error content with retry option.
 */
@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

// Extension functions are now in ProviderOnboardingScreenModel