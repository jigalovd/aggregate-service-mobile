package com.aggregateservice.feature.catalog.navigation

import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.catalog.presentation.screen.CatalogScreen
import com.aggregateservice.feature.catalog.presentation.screen.ProviderDetailScreen
import cafe.adriel.voyager.core.screen.Screen

/**
 * Реализация CatalogNavigator для feature:catalog.
 */
class CatalogNavigatorImpl : CatalogNavigator {
    override fun createCatalogScreen(): Screen {
        return CatalogScreen
    }

    override fun createProviderDetailScreen(providerId: String): Screen {
        return ProviderDetailScreen(providerId = providerId)
    }
}
