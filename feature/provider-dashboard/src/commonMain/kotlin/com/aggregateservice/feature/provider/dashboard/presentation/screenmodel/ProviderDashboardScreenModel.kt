package com.aggregateservice.feature.provider.dashboard.presentation.screenmodel

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import com.aggregateservice.feature.provider.dashboard.domain.repository.ProviderRepository
import com.aggregateservice.feature.provider.dashboard.presentation.model.ProviderDashboardUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана дашборда провайдера.
 *
 * Загружает и агрегирует данные из нескольких источников:
 * - Список бронирований на сегодня
 * - Summary заработков
 * - Статистика провайдера
 *
 * **Architecture:**
 * - Follows BookingHistoryScreenModel pattern
 * - UDF: StateFlow updates only from screenModelScope
 * - Parallel API calls via async/await
 *
 * @property repository Провайдер данных
 * @property logger Логгер для диагностики
 */
@Stable
class ProviderDashboardScreenModel(
    private val repository: ProviderRepository,
    private val logger: Logger,
) : ScreenModel {

    private val _uiState = MutableStateFlow<ProviderDashboardUiState>(ProviderDashboardUiState.Loading)
    val uiState: StateFlow<ProviderDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    /**
     * Начальная загрузка данных дашборда.
     */
    fun loadDashboard() {
        screenModelScope.launch {
            _uiState.update { ProviderDashboardUiState.Loading }

            try {
                // Load all data in parallel using async
                val bookingsDeferred = async { repository.getTodaysBookings() }
                val earningsDeferred = async { repository.getEarningsSummary() }
                val statsDeferred = async { repository.getProviderStats() }

                // Await all results
                val bookingsResult = bookingsDeferred.await()
                val earningsResult = earningsDeferred.await()
                val statsResult = statsDeferred.await()

                // Aggregate errors from all calls
                val errors = mutableListOf<AppError>()
                bookingsResult.onFailure { errors.add(it as? AppError ?: AppError.UnknownError(message = it.message)) }
                earningsResult.onFailure { errors.add(it as? AppError ?: AppError.UnknownError(message = it.message)) }
                statsResult.onFailure { errors.add(it as? AppError ?: AppError.UnknownError(message = it.message)) }

                // Check if ALL calls failed - then show error state
                if (errors.size == 3) {
                    logger.e(errors.first()) { "All dashboard API calls failed" }
                    _uiState.update {
                        ProviderDashboardUiState.Error(
                            error = errors.first(),
                        )
                    }
                    return@launch
                }

                // At least some data available - show content
                val bookings = bookingsResult.getOrDefault(emptyList())
                val earnings = earningsResult.getOrElse { EarningsSummary.empty() }
                val stats = statsResult.getOrElse { ProviderStats.empty() }

                _uiState.update {
                    ProviderDashboardUiState.Content(
                        todaysBookings = bookings,
                        earningsSummary = earnings,
                        providerStats = stats,
                        isRefreshing = false,
                    )
                }

                // Log partial failures
                if (errors.isNotEmpty()) {
                    errors.forEach { error ->
                        logger.w(error) { "Partial dashboard load failure: ${error::class.simpleName}" }
                    }
                }

            } catch (e: Exception) {
                val appError = e as? AppError ?: AppError.UnknownError(throwable = e, message = e.message)
                logger.e(appError) { "Failed to load dashboard: ${appError::class.simpleName}" }
                _uiState.update {
                    ProviderDashboardUiState.Error(error = appError)
                }
            }
        }
    }

    /**
     * Refresh (pull-to-refresh) данные дашборда.
     */
    fun refresh() {
        screenModelScope.launch {
            _uiState.update { state ->
                when (state) {
                    is ProviderDashboardUiState.Loading -> state
                    is ProviderDashboardUiState.Content -> state.copy(isRefreshing = true)
                    is ProviderDashboardUiState.Error -> {
                        // If showing error, refresh from error state
                        ProviderDashboardUiState.Loading
                    }
                }
            }

            try {
                // Check current state to determine if we're already loading
                val currentState = _uiState.value
                if (currentState is ProviderDashboardUiState.Content && currentState.isRefreshing) {
                    // Parallel refresh
                    val bookingsDeferred = async { repository.getTodaysBookings() }
                    val earningsDeferred = async { repository.getEarningsSummary() }
                    val statsDeferred = async { repository.getProviderStats() }

                    val bookingsResult = bookingsDeferred.await()
                    val earningsResult = earningsDeferred.await()
                    val statsResult = statsDeferred.await()

                    val bookings = bookingsResult.getOrDefault(currentState.todaysBookings)
                    val earnings = earningsResult.getOrElse { currentState.earningsSummary }
                    val stats = statsResult.getOrElse { currentState.providerStats }

                    _uiState.update {
                        ProviderDashboardUiState.Content(
                            todaysBookings = bookings,
                            earningsSummary = earnings,
                            providerStats = stats,
                            isRefreshing = false,
                        )
                    }
                } else {
                    // Fallback to full reload
                    loadDashboard()
                }
            } catch (e: Exception) {
                val appError = e as? AppError ?: AppError.UnknownError(throwable = e, message = e.message)
                logger.e(appError) { "Failed to refresh dashboard: ${appError::class.simpleName}" }
                _uiState.update { current ->
                    when (current) {
                        is ProviderDashboardUiState.Content -> {
                            // Keep existing data and just stop refreshing
                            current.copy(isRefreshing = false)
                        }
                        is ProviderDashboardUiState.Error -> {
                            // Already in error state, keep existing data
                            current
                        }
                        is ProviderDashboardUiState.Loading -> {
                            // Shouldn't happen, but handle gracefully
                            ProviderDashboardUiState.Error(error = appError)
                        }
                    }
                }
            }
        }
    }

    /**
     * Retry загрузки данных.
     */
    fun retry() {
        loadDashboard()
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { state ->
            when (state) {
                is ProviderDashboardUiState.Error -> {
                    // Return to loading state for retry
                    ProviderDashboardUiState.Loading
                }
                else -> state
            }
        }
    }
}
