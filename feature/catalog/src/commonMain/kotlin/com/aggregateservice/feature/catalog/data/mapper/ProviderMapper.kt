package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.feature.catalog.data.dto.DayScheduleDto
import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import com.aggregateservice.feature.catalog.data.dto.WorkingHoursDto
import com.aggregateservice.feature.catalog.data.dto.response.LocationDto
import com.aggregateservice.feature.catalog.domain.model.DaySchedule
import com.aggregateservice.feature.catalog.domain.model.Location
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.WorkingHours

/**
 * Mapper для преобразования ProviderDto в Provider.
 *
 * **Important:** Mapper НЕ должен зависеть от platform-специфичного кода.
 */
object ProviderMapper {
    /**
     * Преобразует ProviderDto в Provider.
     *
     * @param dto DTO из API
     * @return Domain model
     */
    fun toDomain(dto: ProviderDto): Provider = Provider(
        id = dto.id,
        userId = dto.userId,
        businessName = dto.businessName,
        description = dto.description,
        logoUrl = dto.logoUrl,
        photos = dto.photos,
        rating = dto.rating,
        reviewCount = dto.reviewCount,
        location = Location(
            latitude = dto.location?.lat ?: 0.0,
            longitude = dto.location?.lon ?: 0.0,
            address = dto.address,
            city = dto.city,
            postalCode = dto.postalCode,
            country = dto.country,
        ),
        workingHours = dto.workingHours?.toDomain() ?: WorkingHours(),
        isVerified = dto.isVerified,
        isActive = dto.isActive,
        createdAt = dto.createdAt,
        categories = dto.categories.map { CategoryMapper.toDomain(it) },
        servicesCount = dto.servicesCount,
    )

    /**
     * Преобразует WorkingHoursDto в WorkingHours.
     */
    private fun WorkingHoursDto.toDomain(): WorkingHours = WorkingHours(
        monday = monday?.toDomain(),
        tuesday = tuesday?.toDomain(),
        wednesday = wednesday?.toDomain(),
        thursday = thursday?.toDomain(),
        friday = friday?.toDomain(),
        saturday = saturday?.toDomain(),
        sunday = sunday?.toDomain(),
    )

    /**
     * Преобразует DayScheduleDto в DaySchedule.
     */
    private fun DayScheduleDto.toDomain(): DaySchedule = DaySchedule(
        openTime = openTime,
        closeTime = closeTime,
        breakStart = breakStart,
        breakEnd = breakEnd,
    )
}
