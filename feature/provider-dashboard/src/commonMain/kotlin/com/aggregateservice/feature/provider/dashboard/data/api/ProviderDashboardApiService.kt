package com.aggregateservice.feature.provider.dashboard.data.api

import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.core.api.models.ProviderResponse
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API Service для Provider Dashboard.
 *
 * Использует существующие booking endpoints как proxy до появления
 * dedicated provider dashboard endpoints.
 *
 * **Note:** Backend извлекает provider_id из JWT токена автоматически
 * (без передачи в URL). API эндпоинты используют авторизацию через
 * Ktor Auth Plugin.
 *
 * @property client HTTP клиент с авторизацией (Ktor Auth Plugin)
 */
class ProviderDashboardApiService(
    private val client: HttpClient,
) {
    /**
     * Получает список бронирований провайдера на сегодня.
     *
     * Proxy endpoint: GET /api/v1/bookings/provider/{id}
     * Использует JWT токен для определения provider_id.
     *
     * @return Result со списком бронирований или ошибкой
     */
    suspend fun getTodaysBookings(): Result<List<BookingResponse>> {
        return safeApiCall<List<BookingResponse>> {
            client.get("/api/v1/bookings/provider/today") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получает список всех бронирований провайдера (для фильтрации на клиенте).
     *
     * Используется как fallback если /provider/today недоступен.
     *
     * @param page Номер страницы (default: 0)
     * @param pageSize Размер страницы (default: 50)
     * @return Result со списком бронирований или ошибкой
     */
    suspend fun getProviderBookings(
        page: Int = 0,
        pageSize: Int = 50,
    ): Result<List<BookingResponse>> {
        return safeApiCall<List<BookingResponse>> {
            client.get("/api/v1/bookings/provider/me") {
                contentType(ContentType.Application.Json)
                url {
                    parameters.append("page", page.toString())
                    parameters.append("pageSize", pageSize.toString())
                }
            }
        }
    }

    /**
     * Получает данные профиля провайдера.
     *
     * Используется для получения статистики (pending requests и т.д.).
     *
     * @return Result с данными провайдера или ошибкой
     */
    suspend fun getProviderProfile(): Result<ProviderResponse> {
        return safeApiCall<ProviderResponse> {
            client.get("/api/v1/providers/me") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}