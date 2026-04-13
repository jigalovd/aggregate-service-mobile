package com.aggregateservice.feature.catalog.domain.model

/**
 * Composite result for provider detail page.
 *
 * Returned by the single /providers/{id}/detail endpoint,
 * replacing 3 separate API calls.
 */
data class ProviderDetailData(
    val provider: Provider,
    val services: List<Service>,
    val isFavorite: Boolean?,
)
