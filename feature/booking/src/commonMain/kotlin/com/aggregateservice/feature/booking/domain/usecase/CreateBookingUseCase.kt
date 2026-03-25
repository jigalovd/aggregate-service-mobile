package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * UseCase для создания нового бронирования.
 *
 * **Business Rules:**
 * - providerId обязателен
 * - Минимум 1 услуга, максимум 10 услуг
 * - Время начала должно быть минимум через 2 часа (US-3.35)
 * - Время начала должно быть в будущем (не более 30 дней, US-3.34)
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

        // Validation: startTime must be at least MIN_BOOKING_NOTICE_HOURS in the future (US-3.35)
        val now = Clock.System.now()
        val minBookingTime = Instant.fromEpochMilliseconds(
            now.toEpochMilliseconds() + MIN_BOOKING_NOTICE_HOURS * 60 * 60 * 1000,
        )
        if (startTime < minBookingTime) {
            return Result.failure(
                AppError.ValidationError(
                    "startTime",
                    "Cannot book less than $MIN_BOOKING_NOTICE_HOURS hours in advance",
                ),
            )
        }

        // Validation: startTime not more than MAX_ADVANCE_DAYS in advance (US-3.34)
        val maxAdvanceTime = Instant.fromEpochMilliseconds(
            now.toEpochMilliseconds() + MAX_ADVANCE_DAYS * 24 * 60 * 60 * 1000,
        )
        if (startTime > maxAdvanceTime) {
            return Result.failure(
                AppError.ValidationError("startTime", "Cannot book more than $MAX_ADVANCE_DAYS days in advance"),
            )
        }

        return repository.createBooking(providerId, serviceIds, startTime, notes)
    }

    companion object {
        /**
         * Максимальное количество услуг в одном бронировании.
         */
        private const val MAX_SERVICES = 10

        /**
         * Максимальное время для бронирования вперед (в днях).
         * US-3.34: Мастер хочет ограничить бронирование 30 днями вперед.
         */
        private const val MAX_ADVANCE_DAYS = 30L

        /**
         * Минимальное время для бронирования (в часах).
         * US-3.35: Мастер хочет запретить бронирование менее чем за 2 часа.
         */
        private const val MIN_BOOKING_NOTICE_HOURS = 2L
    }
}
