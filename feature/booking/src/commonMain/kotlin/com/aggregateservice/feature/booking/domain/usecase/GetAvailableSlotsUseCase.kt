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
 * Поддерживает запрос слотов на диапазон дат (batch loading).
 */
class GetAvailableSlotsUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlot>> {
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.FormValidation("providerId", ValidationRule.Required),
            )
        }

        if (serviceIds.isEmpty()) {
            return Result.failure(
                AppError.FormValidation("serviceIds", ValidationRule.NotEmpty),
            )
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (fromDate < today) {
            return Result.failure(
                AppError.FormValidation("fromDate", ValidationRule.InvalidFormat),
            )
        }

        if (toDate < fromDate) {
            return Result.failure(
                AppError.FormValidation("toDate", ValidationRule.InvalidFormat),
            )
        }

        return repository.getAvailableSlots(providerId, fromDate, toDate, serviceIds)
    }
}
