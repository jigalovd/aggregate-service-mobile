package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.repository.BookingRepository

/**
 * UseCase для получения списка услуг мастера для бронирования.
 *
 * **Feature Isolation:** Использует собственный BookingRepository
 * вместо зависимости от feature:catalog.
 *
 * @property repository Репозиторий бронирований
 */
class GetBookingServicesUseCase(
    private val repository: BookingRepository,
) {
    /**
     * Получает список услуг мастера.
     *
     * @param providerId ID мастера
     * @return Result со списком услуг или ошибка
     */
    suspend operator fun invoke(providerId: String): Result<List<BookingService>> {
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "providerId",
                    message = "Provider ID cannot be empty",
                ),
            )
        }

        return repository.getProviderServices(providerId)
    }
}
