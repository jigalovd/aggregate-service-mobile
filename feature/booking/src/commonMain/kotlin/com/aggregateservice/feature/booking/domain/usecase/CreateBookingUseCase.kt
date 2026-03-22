package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.Instant

/**
 * UseCase для создания нового бронирования.
 *
 * **Business Rules:**
 * - providerId обязателен
 * - Минимум 1 услуга, максимум 10 услуг
 * - Время начала должно быть в будущем (не более 30 дней)
 *
 * **Usage:**
 * ```kotlin
 * val result = createBookingUseCase(
 *     providerId = "provider-123",
 *     serviceIds = listOf("service-1", "service-2"),
 *     startTime = Instant.parse("2026-03-25T14:00:00Z"),
 *     notes = "Женская стрижка + укладка"
 * )
 * ```
 */
class CreateBookingUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        serviceIds: List<String>,
        startTime: Instant,
        notes: String? = null,
    ): Result<Booking> {
        // Validation: providerId
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.ValidationError("providerId", "Provider ID is required"),
            )
        }

        // Validation: serviceIds
        if (serviceIds.isEmpty()) {
            return Result.failure(
                AppError.ValidationError("serviceIds", "At least one service is required"),
            )
        }
        if (serviceIds.size > MAX_SERVICES) {
            return Result.failure(
                AppError.ValidationError("serviceIds", "Maximum $MAX_SERVICES services allowed"),
            )
        }

        // Validation: startTime must be in the future
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        if (startTime <= now) {
            return Result.failure(
                AppError.ValidationError("startTime", "Start time must be in the future"),
            )
        }

        // Validation: startTime not more than 30 days in advance
        val maxAdvanceTime = Instant.fromEpochMilliseconds(
            now.toEpochMilliseconds() + MAX_ADVANCE_DAYS_DAYS * 24 * 60 * 60 * 1000
        )
        if (startTime > maxAdvanceTime) {
            return Result.failure(
                AppError.ValidationError("startTime", "Cannot book more than $MAX_ADVANCE_DAYS_DAYS days in advance"),
            )
        }

        return repository.createBooking(providerId, serviceIds, startTime, notes)
    }

    companion object {
        private const val MAX_SERVICES = 10
        private const val MAX_ADVANCE_DAYS_DAYS = 30L
    }
}
