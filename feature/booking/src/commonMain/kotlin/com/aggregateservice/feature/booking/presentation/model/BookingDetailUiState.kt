package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking

@Stable
data class BookingDetailUiState(
    val booking: Booking? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val isCancelling: Boolean = false,
    val cancelError: AppError? = null,
) {
    val isLoaded: Boolean get() = booking != null && !isLoading

    companion object {
        val Loading = BookingDetailUiState(isLoading = true)

        fun error(error: AppError) = BookingDetailUiState(isLoading = false, error = error)
    }
}
