package com.aggregateservice.feature.catalog.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.Service

/**
 * UI State для экрана деталей мастера (Presentation слой).
 *
 * **UDF Pattern:**
 * - Immutable state class
 * - ScreenModel создает новые экземпляры при изменении
 * - Compose реагирует на изменения через StateFlow
 *
 * @property provider Детали мастера (null если загружается)
 * @property services Список услуг мастера
 * @property isLoading Состояние начальной загрузки
 * @property isLoadingServices Загрузка услуг
 * @property isLoaded Данные успешно загружены
 * @property error Ошибка загрузки (null если нет ошибки)
 * @property selectedCategoryId Выбранная категория для фильтрации услуг
 * @property isFavorite Добавлен ли мастер в избранное
 */
@Stable
data class ProviderDetailUiState(
    val provider: Provider? = null,
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingServices: Boolean = false,
    val error: AppError? = null,
    val selectedCategoryId: String? = null,
    val isFavorite: Boolean = false,
    val selectedServiceIds: Set<String> = emptySet(),
) {
    /**
     * Проверяет, загружены ли данные.
     */
    val isLoaded: Boolean
        get() = provider != null && !isLoading

    /**
     * Возвращает услуги, отфильтрованные по категории.
     */
    val filteredServices: List<Service>
        get() {
            val byCategory =
                if (selectedCategoryId != null) {
                    services.filter { it.categoryId == selectedCategoryId }
                } else {
                    services
                }
            if (selectedServiceIds.isEmpty()) return byCategory

            val selectedServices = byCategory.filter { it.id in selectedServiceIds }
            val hasNonCombinable = selectedServices.any { !it.isCombinable }

            return if (hasNonCombinable) {
                byCategory.filter { it.id in selectedServiceIds }
            } else {
                byCategory.filter { it.isCombinable || it.id in selectedServiceIds }
            }
        }

    val selectedServices: List<Service>
        get() = services.filter { it.id in selectedServiceIds }

    val totalPrice: Double
        get() = selectedServices.sumOf { it.price.amount }

    val totalDurationMinutes: Int
        get() = selectedServices.sumOf { it.durationMinutes }

    /**
     * Возвращает уникальные категории из услуг как List<Pair<categoryId, categoryName>>.
     * Note: categoryName извлекается из первой услуги каждой категории.
     */
    val serviceCategories: List<Pair<String, String>>
        get() =
            services
                .groupBy { it.categoryId }
                .map { (categoryId, servicesInCategory) ->
                    // Use first service name as category name fallback
                    categoryId to categoryId.replace("_", " ").replaceFirstChar { it.uppercase() }
                }

    /**
     * Проверяет, открыт ли мастер сейчас.
     */
    val isOpenNow: Boolean
        get() = provider?.workingHours?.isOpen ?: false

    companion object {
        /**
         * Initial state при загрузке.
         */
        val Loading =
            ProviderDetailUiState(
                isLoading = true,
            )

        /**
         * Создает state с ошибкой.
         */
        fun error(error: AppError) =
            ProviderDetailUiState(
                isLoading = false,
                error = error,
            )
    }
}
