package com.aggregateservice.feature.favorites.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.usecase.GetFavoritesUseCase
import com.aggregateservice.feature.favorites.domain.usecase.RemoveFavoriteUseCase
import com.aggregateservice.feature.favorites.presentation.model.FavoritesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for favorites screen.
 *
 * **Responsibilities:**
 * - Loading user's favorites
 * - Managing remove confirmation
 * - Handling remove action
 *
 * @property getFavoritesUseCase UseCase for loading favorites
 * @property removeFavoriteUseCase UseCase for removing favorite
 */
class FavoritesScreenModel(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    /**
     * Loads all favorites for the user.
     */
    fun loadFavorites() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getFavoritesUseCase().fold(
                onSuccess = { favorites ->
                    _uiState.update {
                        FavoritesUiState(
                            favorites = favorites,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Failed to load favorites: ${appError::class.simpleName}" }
                    _uiState.update {
                        FavoritesUiState.error(appError)
                    }
                },
            )
        }
    }

    /**
     * Shows remove confirmation dialog for a favorite.
     *
     * @param favorite Favorite to remove
     */
    fun confirmRemove(favorite: Favorite) {
        _uiState.update { it.copy(favoriteToRemove = favorite) }
    }

    /**
     * Dismisses remove confirmation dialog.
     */
    fun dismissRemoveDialog() {
        _uiState.update { it.copy(favoriteToRemove = null) }
    }

    /**
     * Removes the favorite pending confirmation.
     */
    fun removeFavorite() {
        val favoriteToRemove = _uiState.value.favoriteToRemove ?: return

        screenModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }

            removeFavoriteUseCase(favoriteToRemove.providerId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            favorites = state.favorites.filter { it.providerId != favoriteToRemove.providerId },
                            favoriteToRemove = null,
                            isRemoving = false,
                        )
                    }
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Failed to remove favorite: ${appError::class.simpleName}" }
                    _uiState.update { state ->
                        state.copy(
                            favoriteToRemove = null,
                            isRemoving = false,
                            error = appError,
                        )
                    }
                },
            )
        }
    }

    /**
     * Clears the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
