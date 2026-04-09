package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository

/**
 * UseCase для получения списка услуг мастера (Domain слой).
 *
 * **Responsibilities:**
 * - Валидация ID мастера
 * - Получение услуг из репозитория
 * - Фильтрация по категории
 *
 * @property repository Репозиторий каталога
 */
class GetProviderServicesUseCase(
    private val repository: CatalogRepository,
) {
    /**
     * Получает список услуг мастера.
     *
     * @param providerId ID мастера
     * @param categoryId ID категории для фильтрации (опционально)
     * @return Result с List<Service> при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(
        providerId: String,
        categoryId: String? = null,
    ): Result<List<Service>> {
        // Валидация ID
        if (providerId.isBlank()) {
            return Result.failure(
                AppError.FormValidation("providerId", ValidationRule.NotBlank),
            )
        }

        // Вызов репозитория
        return repository.getProviderServices(providerId, categoryId)
    }
}
