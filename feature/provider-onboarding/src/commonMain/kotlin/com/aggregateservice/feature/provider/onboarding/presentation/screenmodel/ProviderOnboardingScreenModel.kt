package com.aggregateservice.feature.provider.onboarding.presentation.screenmodel

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.auth.contract.SwitchRoleUseCase
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.storage.TokenStore
import com.aggregateservice.feature.provider.onboarding.OnboardingState
import com.aggregateservice.feature.provider.onboarding.domain.repository.ProviderOnboardingRepository
import com.aggregateservice.feature.provider.onboarding.presentation.model.OnboardingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Stable
class ProviderOnboardingScreenModel(
    private val repository: ProviderOnboardingRepository,
    private val tokenStore: TokenStore,
    private val switchRoleUseCase: SwitchRoleUseCase,
    private val logger: Logger,
) : ScreenModel {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.content())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _basicInfo = MutableStateFlow(OnboardingState.BasicInfoStep())
    private val _location = MutableStateFlow(OnboardingState.LocationStep())
    private val _services = MutableStateFlow(OnboardingState.ServicesStep())

    var onComplete: (() -> Unit)? = null

    init {
        logger.d("ProviderOnboarding") { "ScreenModel initialized" }
    }

    fun nextStep() {
        val currentState = _uiState.value
        if (currentState !is OnboardingUiState.Content) return
        if (currentState.isSubmitting) return

        val currentStepErrors = validateCurrentStep()
        if (currentStepErrors.isNotEmpty()) {
            _uiState.value = currentState.copy(validationErrors = currentStepErrors, isValid = false)
            return
        }

        if (currentState.step < OnboardingState.LAST_STEP_INDEX) {
            _uiState.value = currentState.copy(step = currentState.step + 1, isValid = false, validationErrors = emptyMap())
        } else {
            submitOnboarding()
        }
    }

    fun previousStep() {
        val currentState = _uiState.value
        if (currentState !is OnboardingUiState.Content) return
        if (currentState.isSubmitting) return
        if (currentState.step <= 0) return
        _uiState.value = currentState.copy(step = currentState.step - 1, validationErrors = emptyMap())
    }

    fun updateBasicInfo(businessName: String? = null, bio: String? = null, phone: String? = null) {
        val current = _basicInfo.value
        val filteredPhone = phone?.filter { it.isDigit() }
        _basicInfo.value = current.copy(
            businessName = businessName ?: current.businessName,
            bio = bio ?: current.bio,
            phone = filteredPhone ?: current.phone,
        )
        validateAndUpdateStateForStep(0)
    }

    fun updateLocation(address: String? = null, serviceRadiusKm: Float? = null) {
        val current = _location.value
        _location.value = current.copy(
            address = address ?: current.address,
            serviceRadiusKm = serviceRadiusKm ?: current.serviceRadiusKm,
        )
        validateAndUpdateStateForStep(1)
    }

    fun toggleCategory(categoryId: String) {
        val current = _services.value
        _services.value = if (categoryId in current.selectedCategoryIds) {
            current.copy(selectedCategoryIds = current.selectedCategoryIds - categoryId)
        } else {
            current.copy(selectedCategoryIds = current.selectedCategoryIds + categoryId)
        }
        validateAndUpdateStateForStep(2)
    }

    fun setCategories(categoryIds: Set<String>) {
        _services.value = _services.value.copy(selectedCategoryIds = categoryIds)
        validateAndUpdateStateForStep(2)
    }

    private fun validateAndUpdateStateForStep(step: Int) {
        val currentState = _uiState.value
        if (currentState !is OnboardingUiState.Content) return

        val errors: Map<String, String> = when (step) {
            0 -> validateBasicInfo(_basicInfo.value)
            1 -> validateLocation(_location.value)
            2 -> validateServices(_services.value)
            else -> emptyMap()
        }

        _uiState.value = currentState.copy(isValid = errors.isEmpty(), validationErrors = errors)
    }

    private fun validateCurrentStep(): Map<String, String> {
        val currentState = _uiState.value
        if (currentState !is OnboardingUiState.Content) return emptyMap()
        return when (currentState.step) {
            0 -> validateBasicInfo(_basicInfo.value)
            1 -> validateLocation(_location.value)
            2 -> validateServices(_services.value)
            else -> emptyMap()
        }
    }

    private fun validateBasicInfo(info: OnboardingState.BasicInfoStep): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (info.businessName.isBlank()) errors["businessName"] = "Business name is required"
        if (info.phone.length < OnboardingState.BasicInfoStep.MIN_PHONE_LENGTH) {
            errors["phone"] = "Phone must be at least ${OnboardingState.BasicInfoStep.MIN_PHONE_LENGTH} digits"
        }
        return errors
    }

    private fun validateLocation(location: OnboardingState.LocationStep): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (location.address.isBlank()) errors["address"] = "Service address is required"
        if (location.serviceRadiusKm < OnboardingState.LocationStep.MIN_RADIUS_KM) {
            errors["serviceRadius"] = "Service radius must be at least ${OnboardingState.LocationStep.MIN_RADIUS_KM.toInt()} km"
        }
        return errors
    }

    private fun validateServices(services: OnboardingState.ServicesStep): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (services.selectedCategoryIds.size < OnboardingState.ServicesStep.MIN_CATEGORIES) {
            errors["categories"] = "Select at least ${OnboardingState.ServicesStep.MIN_CATEGORIES} category"
        }
        return errors
    }

    fun submitOnboarding() {
        val currentState = _uiState.value
        if (currentState !is OnboardingUiState.Content) return
        if (currentState.isSubmitting) return

        val allErrors = validateBasicInfo(_basicInfo.value) + validateLocation(_location.value) + validateServices(_services.value)
        if (allErrors.isNotEmpty()) {
            _uiState.value = currentState.copy(isValid = false, validationErrors = allErrors)
            return
        }

        _uiState.value = currentState.copy(isSubmitting = true, validationErrors = emptyMap())

        screenModelScope.launch {
            repository.submitOnboarding(
                businessName = _basicInfo.value.businessName,
                bio = _basicInfo.value.bio.take(OnboardingState.BasicInfoStep.MAX_BIO_LENGTH),
                phone = _basicInfo.value.phone,
                address = _location.value.address,
                serviceRadiusKm = _location.value.serviceRadiusKm,
                categoryIds = _services.value.selectedCategoryIds.toList(),
            ).fold(
                onSuccess = { response ->
                    // Save new access token
                    val refreshToken = tokenStore.getRefreshToken() ?: ""
                    tokenStore.saveTokens(
                        accessToken = response.accessToken,
                        refreshToken = refreshToken,
                    )

                    // Switch to PROVIDER role
                    switchRoleUseCase("PROVIDER")

                    logger.d("ProviderOnboarding") { "ProviderOnboarding: role switch completed" }

                    _uiState.value = OnboardingUiState.Content(step = 3, isValid = true, isSubmitting = false, isSubmitted = true)
                    onComplete?.invoke()
                },
                onFailure = { error ->
                    val appError = error as? AppError ?: AppError.UnknownError(message = error.message)
                    _uiState.value = OnboardingUiState.Error(error = appError, canRetry = true)
                },
            )
        }
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState is OnboardingUiState.Error && currentState.canRetry) {
            submitOnboarding()
        }
    }

    fun getBasicInfo(): OnboardingState.BasicInfoStep = _basicInfo.value
    fun getLocation(): OnboardingState.LocationStep = _location.value
    fun getServices(): OnboardingState.ServicesStep = _services.value
}