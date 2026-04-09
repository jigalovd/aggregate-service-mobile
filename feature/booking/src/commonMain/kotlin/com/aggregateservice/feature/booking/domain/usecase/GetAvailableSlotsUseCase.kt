@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * UseCase для получения доступных временных слотов.
 *
 * Слоты генерируются на основе:
 * - Рабочего расписания мастера
 * - Уже забронированных слотов
 * - Длительности выбранных услуг
 *
 * **Usage:**
 * ```kotlin
 * val slots = getAvailableSlotsUseCase(
 *     providerId = "provider-123",
 *     date = LocalDate(2026, 3, 25),
 *     serviceIds = listOf("service-1", "service-2")
 * )
 * slots.fold(
 *     onSuccess = { availableSlots -> showTimeSlots(availableSlots) },
 *     onFailure = { error -> showError(error) }
 * )
 * ```
 */
class GetAvailableSlotsUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        date: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlot>> {
        // Validation: providerId
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.FormValidation("providerId", ValidationRule.Required),
            )
        }

        // Validation: serviceIds
        if (serviceIds.isEmpty()) {
            return Result.failure(
                AppError.FormValidation("serviceIds", ValidationRule.NotEmpty),
            )
        }

        // Validation: date not in the past
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (date < today) {
            return Result.failure(
                AppError.FormValidation("date", ValidationRule.InvalidFormat),
            )
        }

        return repository.getAvailableSlots(providerId, date, serviceIds)
    }
}
