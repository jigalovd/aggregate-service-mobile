package com.aggregateservice.feature.favorites.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.favorites.domain.model.Favorite

/**
 * UI State for favorites screen.
 *
 * **UDF Pattern:** Immutable state, updated through ScreenModel.
 *
 * @property favorites List of favorite providers
 * @property isLoading Loading state
 * @property isRemoving Removing in progress
 * @property error Error state
 * @property favoriteToRemove Favorite pending removal (for confirmation dialog)
 */
@Stable
data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = true,
    val isRemoving: Boolean = false,
    val error: AppError? = null,
    val favoriteToRemove: Favorite? = null,
) {
    /**
     * Whether there are any favorites.
     */
    val hasFavorites: Boolean
        get() = favorites.isNotEmpty()

    /**
     * Number of favorite providers.
     */
    val favoritesCount: Int
        get() = favorites.size

    /**
     * Whether remove confirmation dialog should be shown.
     */
    val showRemoveDialog: Boolean
        get() = favoriteToRemove != null

    companion object {
        /**
         * Initial loading state.
         */
        val Loading = FavoritesUiState(isLoading = true)

        /**
         * Error state.
         */
        fun error(error: AppError) = FavoritesUiState(
            isLoading = false,
            error = error,
        )
    }
}
