package com.aggregateservice.feature.catalog.domain.model

/**
 * Чистая доменная модель мастера (Provider).
 *
 * Provider - это независимый профессионал, предоставляющий услуги.
 * Может иметь несколько услуг (Service) и категории.
 *
 * **Important:** Этот класс НЕ содержит никаких платформенных зависимостей
 * или DTO из network слоя. Только чистые бизнес-данные.
 *
 * @property id Уникальный идентификатор мастера
 * @property userId ID пользователя (owner)
 * @property businessName Название бизнеса/салона
 * @property description Описание бизнеса (i18n)
 * @property logoUrl URL логотипа (опционально)
 * @property photos URL фотографий (опционально)
 * @property rating Средний рейтинг
 * @property reviewCount Количество отзывов
 * @property location Геолокация мастера
 * @property workingHours Часы работы
 * @property isVerified Подтверждён ли верифицирован
 * @property isActive Активен ли профиль
 * @property createdAt Дата создания профиля
 * @property categories Список категорий услуг
 * @property servicesCount Количество услуг
 */
data class Provider(
    val id: String,
    val userId: String,
    val businessName: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val photos: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val location: Location,
    val workingHours: WorkingHours,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: kotlinx.datetime.Instant,
    val categories: List<Category> = emptyList(),
    val servicesCount: Int = 0,
) {
    /**
     * Средний рейтинг в формате "X.X" или "X.XX".
     */
    val formattedRating: String
        get() = "%.1f".format(rating)

    /**
     * URL первого фото или placeholder.
     */
    val primaryPhotoUrl: String?
        get() = photos.firstOrNull() ?: logoUrl

    /**
     * Короткое описание для карточки (первые 100 символов description).
     */
    val shortDescription: String?
        get() = description?.take(100)
}
