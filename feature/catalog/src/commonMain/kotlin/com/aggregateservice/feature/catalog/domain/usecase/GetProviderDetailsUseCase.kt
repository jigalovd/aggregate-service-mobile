package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository

/**
 * UseCase для получения детальной информации о мастере (Domain слой).
 *
 * **Responsibilities:**
 * - Валидация ID мастера
 * - Вызов репозитория
 * - Кэширование (в будущем)
 *
 * @property repository Репозиторий каталога
 */
class GetProviderDetailsUseCase(
    private val repository: CatalogRepository,
) {
    /**
     * Получает детали мастера по ID.
     *
     * @param providerId ID мастера
     * @return Result с Provider при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(providerId: String): Result<Provider> {
        // Валидация ID
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "providerId",
                    message = "Provider ID cannot be empty",
                ),
            )
        }

        // Вызов репозитория
        return repository.getProviderById(providerId)
    }
}
