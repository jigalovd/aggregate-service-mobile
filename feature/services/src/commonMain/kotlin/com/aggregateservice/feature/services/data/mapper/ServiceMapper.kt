package com.aggregateservice.feature.services.data.mapper

import com.aggregateservice.core.api.models.I18nStringSchema
import com.aggregateservice.core.api.models.ProviderServiceCreateRequest
import com.aggregateservice.core.api.models.ProviderServiceResponse
import com.aggregateservice.core.api.models.ProviderServiceUpdateRequest
import com.aggregateservice.core.i18n.AppLocale
import com.aggregateservice.feature.services.domain.model.CreateServiceRequest
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.model.UpdateServiceRequest

/**
 * Mapper for converting between Service DTOs and Domain models.
 */
object ServiceMapper {

    /**
     * Converts ProviderServiceResponse DTO to domain model.
     *
     * Note: ProviderServiceResponse contains a nested ServiceResponse (from catalog)
     * representing the base service definition. We extract the localized title.
     */
    fun toDomain(dto: ProviderServiceResponse): ProviderService {
        val service = dto.service
        val title = service?.title?.getLocalized(AppLocale.EN) ?: "Service ${dto.serviceId}"
        val description = service?.description?.getLocalizedOrNull()

        return ProviderService(
            id = dto.id,
            name = title,
            description = description,
            basePrice = (dto.customPrice ?: service?.basePrice ?: 0) / 100.0,
            durationMinutes = service?.durationMinutes ?: 0,
            categoryId = service?.categoryId ?: "",
            isActive = dto.isActive ?: true,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )
    }

    /**
     * Converts list of DTOs to list of domain models.
     */
    fun toDomain(dtos: List<ProviderServiceResponse>): List<ProviderService> =
        dtos.map { toDomain(it) }

    /**
     * Converts domain CreateServiceRequest to API request DTO.
     *
     * Note: ProviderServiceCreateRequest only contains serviceId, customPrice, isActive.
     * The base service must be pre-created via backend catalog first.
     */
    fun toDto(request: CreateServiceRequest): ProviderServiceCreateRequest =
        ProviderServiceCreateRequest(
            serviceId = request.categoryId,
            customPrice = (request.basePrice * 100).toInt(),
            isActive = true,
        )

    /**
     * Converts domain UpdateServiceRequest to API request DTO.
     */
    fun toDto(request: UpdateServiceRequest): ProviderServiceUpdateRequest =
        ProviderServiceUpdateRequest(
            customPrice = request.basePrice?.let { (it * 100).toInt() },
            isActive = request.isActive,
        )
}

/**
 * Get localized string from I18nStringSchema.
 * Falls back to English if locale not found.
 */
internal fun I18nStringSchema.getLocalized(locale: AppLocale): String =
    when (locale) {
        AppLocale.RU -> ru
        AppLocale.HE -> he
        AppLocale.EN -> en
    }

/**
 * Get localized string or null if all values are null.
 */
internal fun I18nStringSchema.getLocalizedOrNull(): String? =
    ru ?: he ?: en
