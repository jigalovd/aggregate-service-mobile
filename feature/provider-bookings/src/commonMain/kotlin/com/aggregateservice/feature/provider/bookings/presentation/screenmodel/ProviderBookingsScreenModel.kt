package com.aggregateservice.feature.provider.bookings.presentation.screenmodel

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.bookings.domain.model.BookingFilter
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking
import com.aggregateservice.feature.provider.bookings.domain.repository.ProviderBookingRepository
import com.aggregateservice.feature.provider.bookings.presentation.model.ProviderBookingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана управления бронированиями провайдера.
 *
 * Загружает список всех бронирований провайдера с поддержкой:
 * - Фильтрации по статусу
 * - Действий (принять/отклонить/отменить)
 * - Пагинации
 * - Pull-to-refresh
 *
 * **Architecture:**
 * - UDF: StateFlow updates only from screenModelScope
 * - Error propagation with AppError subtypes
 * - Action results reflected via state refresh
 *
 * @property repository Провайдер данных для бронирований
 * @property logger Логгер для observability (tag: "ProviderBookings")
 */
@Stable
class ProviderBookingsScreenModel(
    private val repository: ProviderBookingRepository,
    private val logger: Logger,
) : ScreenModel {

    private val _uiState = MutableStateFlow<ProviderBookingsUiState>(ProviderBookingsUiState.Loading)
    val uiState: StateFlow<ProviderBookingsUiState> = _uiState.asStateFlow()

    // Pagination state
    private var currentPage = 1
    private val pageSize = 20
    private var hasMorePages = true

    init {
        loadBookings()
    }

    /**
     * Начальная загрузка бронирований.
     */
    fun loadBookings() {
        screenModelScope.launch {
            currentPage = 1
            hasMorePages = true
            _uiState.update { ProviderBookingsUiState.Loading }

            loadBookingsInternal(isRefresh = false)
        }
    }

    /**
     * Обновление списка бронирований (pull-to-refresh).
     */
    fun refresh() {
        screenModelScope.launch {
            val currentState = _uiState.value

            when (currentState) {
                is ProviderBookingsUiState.Loading -> {
                    // Already loading, no-op
                    logger.d("ProviderBookings") { "refresh() called but already loading" }
                }
                is ProviderBookingsUiState.Content -> {
                    _uiState.update { currentState.copy(isRefreshing = true) }
                    currentPage = 1
                    hasMorePages = true
                    loadBookingsInternal(isRefresh = true)
                }
                is ProviderBookingsUiState.Error -> {
                    // From error state, do full reload
                    currentPage = 1
                    hasMorePages = true
                    _uiState.update { ProviderBookingsUiState.Loading }
                    loadBookingsInternal(isRefresh = false)
                }
            }
        }
    }

    /**
     * Повторить загрузку после ошибки.
     */
    fun retry() {
        loadBookings()
    }

    /**
     * Изменить фильтр статуса и перезагрузить данные.
     */
    fun filterBookings(filter: BookingFilter) {
        screenModelScope.launch {
            val currentState = _uiState.value

            // Store current filter if in content state
            val currentFilter = when (currentState) {
                is ProviderBookingsUiState.Content -> currentState.selectedFilter
                is ProviderBookingsUiState.Error -> currentState.selectedFilter
                else -> BookingFilter.ALL
            }

            currentPage = 1
            hasMorePages = true

            // Update filter and show loading
            _uiState.update {
                ProviderBookingsUiState.Content(
                    bookings = emptyList(),
                    selectedFilter = filter,
                    isRefreshing = false,
                    isLoadingAction = false,
                    actionError = null,
                )
            }

            // If filter changed, reload
            if (currentFilter != filter) {
                loadBookingsInternal(isRefresh = false)
            }
        }
    }

    /**
     * Принять бронирование.
     *
     * @param bookingId ID бронирования
     */
    fun acceptBooking(bookingId: String) {
        screenModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ProviderBookingsUiState.Content) {
                logger.w("ProviderBookings") { "acceptBooking called but state is not Content" }
                return@launch
            }

            // Show action loading
            _uiState.update { currentState.copy(isLoadingAction = true, actionError = null) }

            repository.acceptBooking(bookingId).fold(
                onSuccess = {
                    logger.d("ProviderBookings") { "acceptBooking: success for $bookingId" }
                    // Refresh to update the list
                    refreshAfterAction()
                },
                onFailure = { error ->
                    val appError = error as? AppError ?: AppError.UnknownError(message = error.message)
                    logger.e("ProviderBookings") { "acceptBooking: failed for $bookingId - ${appError::class.simpleName}" }
                    _uiState.update { state ->
                        if (state is ProviderBookingsUiState.Content) {
                            state.copy(
                                isLoadingAction = false,
                                actionError = appError.message ?: "Failed to accept booking",
                            )
                        } else state
                    }
                },
            )
        }
    }

    /**
     * Отклонить бронирование.
     *
     * @param bookingId ID бронирования
     * @param reason Причина отклонения
     */
    fun rejectBooking(bookingId: String, reason: String) {
        screenModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ProviderBookingsUiState.Content) {
                logger.w("ProviderBookings") { "rejectBooking called but state is not Content" }
                return@launch
            }

            _uiState.update { currentState.copy(isLoadingAction = true, actionError = null) }

            repository.rejectBooking(bookingId, reason).fold(
                onSuccess = {
                    logger.d("ProviderBookings") { "rejectBooking: success for $bookingId" }
                    refreshAfterAction()
                },
                onFailure = { error ->
                    val appError = error as? AppError ?: AppError.UnknownError(message = error.message)
                    logger.e("ProviderBookings") { "rejectBooking: failed for $bookingId - ${appError::class.simpleName}" }
                    _uiState.update { state ->
                        if (state is ProviderBookingsUiState.Content) {
                            state.copy(
                                isLoadingAction = false,
                                actionError = appError.message ?: "Failed to reject booking",
                            )
                        } else state
                    }
                },
            )
        }
    }

    /**
     * Отменить бронирование.
     *
     * @param bookingId ID бронирования
     * @param reason Причина отмены (опционально)
     */
    fun cancelBooking(bookingId: String, reason: String? = null) {
        screenModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ProviderBookingsUiState.Content) {
                logger.w("ProviderBookings") { "cancelBooking called but state is not Content" }
                return@launch
            }

            _uiState.update { currentState.copy(isLoadingAction = true, actionError = null) }

            repository.cancelBooking(bookingId, reason).fold(
                onSuccess = {
                    logger.d("ProviderBookings") { "cancelBooking: success for $bookingId" }
                    refreshAfterAction()
                },
                onFailure = { error ->
                    val appError = error as? AppError ?: AppError.UnknownError(message = error.message)
                    logger.e("ProviderBookings") { "cancelBooking: failed for $bookingId - ${appError::class.simpleName}" }
                    _uiState.update { state ->
                        if (state is ProviderBookingsUiState.Content) {
                            state.copy(
                                isLoadingAction = false,
                                actionError = appError.message ?: "Failed to cancel booking",
                            )
                        } else state
                    }
                },
            )
        }
    }

    /**
     * Загрузить следующую страницу бронирований.
     */
    fun loadMoreBookings() {
        screenModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ProviderBookingsUiState.Content) return@launch
            if (!hasMorePages) {
                logger.d("ProviderBookings") { "loadMoreBookings: no more pages to load" }
                return@launch
            }

            currentPage++
            loadBookingsInternal(isRefresh = true, append = true)
        }
    }

    /**
     * Очистить ошибку действия.
     */
    fun clearActionError() {
        _uiState.update { state ->
            if (state is ProviderBookingsUiState.Content) {
                state.copy(actionError = null)
            } else state
        }
    }

    /**
     * Внутренняя функция загрузки бронирований.
     */
    private suspend fun loadBookingsInternal(isRefresh: Boolean, append: Boolean = false) {
        val currentState = _uiState.value
        val selectedFilter = when (currentState) {
            is ProviderBookingsUiState.Content -> currentState.selectedFilter
            is ProviderBookingsUiState.Error -> currentState.selectedFilter
            else -> BookingFilter.ALL
        }

        val statusParam = selectedFilter.status?.name

        logger.d("ProviderBookings") {
            "loadBookingsInternal: page=$currentPage, status=$statusParam, append=$append, refresh=$isRefresh"
        }

        repository.getProviderBookings(
            status = statusParam,
            page = currentPage,
            pageSize = pageSize,
        ).fold(
            onSuccess = { bookings ->
                hasMorePages = bookings.size >= pageSize

                logger.d("ProviderBookings") {
                    "loadBookingsInternal: loaded ${bookings.size} bookings, hasMore=$hasMorePages"
                }

                _uiState.update { state ->
                    val existingBookings = if (append && state is ProviderBookingsUiState.Content) {
                        state.bookings
                    } else {
                        emptyList()
                    }

                    ProviderBookingsUiState.Content(
                        bookings = existingBookings + bookings,
                        selectedFilter = selectedFilter,
                        isRefreshing = false,
                        isLoadingAction = false,
                        actionError = null,
                    )
                }
            },
            onFailure = { error ->
                val appError = error as? AppError ?: AppError.UnknownError(message = error.message)
                logger.e("ProviderBookings") {
                    "loadBookingsInternal: failed - ${appError::class.simpleName}: ${appError.message}"
                }

                _uiState.update { state ->
                    val cachedBookings = if (state is ProviderBookingsUiState.Content) {
                        state.bookings
                    } else if (state is ProviderBookingsUiState.Error) {
                        state.bookings
                    } else {
                        emptyList()
                    }

                    ProviderBookingsUiState.Error(
                        error = appError,
                        bookings = cachedBookings,
                        selectedFilter = selectedFilter,
                    )
                }
            },
        )
    }

    /**
     * Обновить список после выполнения действия.
     */
    private suspend fun refreshAfterAction() {
        currentPage = 1
        hasMorePages = true
        loadBookingsInternal(isRefresh = false)
    }
}
