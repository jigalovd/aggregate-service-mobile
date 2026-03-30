package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository

/**
 * UseCase для получения истории бронирований клиента.
 *
 * **Usage:**
 * ```kotlin
 * // Получить все бронирования
 * val allBookings = getClientBookingsUseCase("client-123")
 *
 * // Получить только предстоящие
 * val upcoming = getClientBookingsUseCase(
 *     clientId = "client-123",
 *     status = "CONFIRMED"
 * )
 * ```
 */
class GetClientBookingsUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<List<Booking>> {
        // Validation: page
        if (page < 1) {
            return Result.failure(
                AppError.ValidationError("page", "Page must be >= 1"),
            )
        }

        // Validation: pageSize
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return Result.failure(
                AppError.ValidationError("pageSize", "Page size must be between 1 and $MAX_PAGE_SIZE"),
            )
        }

        return repository.getClientBookings(status, page, pageSize)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}
