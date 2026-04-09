package com.aggregateservice.feature.favorites.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.presentation.model.FavoritesUiState
import com.aggregateservice.feature.favorites.presentation.screenmodel.FavoritesScreenModel
import org.koin.compose.koinInject

/**
 * Favorites screen displaying user's favorite providers.
 */
object FavoritesScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<FavoritesScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val i18nProvider: I18nProvider = koinInject()
        val catalogNavigator: CatalogNavigator = koinInject()
        val authNavigator: AuthNavigator = koinInject()
        val authStateProvider: AuthStateProvider = koinInject()
        val authState by authStateProvider.authState.collectAsState()
        val isAuthenticated = authState is AuthState.Authenticated

        // Navigate to login screen when not authenticated
        LaunchedEffect(isAuthenticated) {
            if (!isAuthenticated) {
                navigator.push(authNavigator.createLoginScreen())
            }
        }

        // Load favorites when authenticated
        LaunchedEffect(isAuthenticated) {
            if (isAuthenticated) {
                screenModel.loadFavorites()
            }
        }

        FavoritesScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onRefresh = { screenModel.loadFavorites() },
            onFavoriteClick = { favorite ->
                navigator.push(catalogNavigator.createProviderDetailScreen(favorite.providerId))
            },
            onRemoveClick = { screenModel.confirmRemove(it) },
            onConfirmRemove = { screenModel.removeFavorite() },
            onDismissRemove = { screenModel.dismissRemoveDialog() },
            onClearError = { screenModel.clearError() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    i18nProvider: I18nProvider,
    uiState: FavoritesUiState,
    onRefresh: () -> Unit,
    onFavoriteClick: (Favorite) -> Unit,
    onRemoveClick: (Favorite) -> Unit,
    onConfirmRemove: () -> Unit,
    onDismissRemove: () -> Unit,
    onClearError: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isLoading && uiState.favorites.isNotEmpty(),
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                uiState.isLoading && uiState.favorites.isEmpty() -> {
                    LoadingState()
                }

                uiState.error != null && uiState.favorites.isEmpty() -> {
                    ErrorState(
                        message = uiState.error!!.message ?: i18nProvider[StringKey.Error.UNKNOWN],
                        onRetry = onRefresh,
                    )
                }

                !uiState.hasFavorites -> {
                    EmptyState(i18nProvider = i18nProvider)
                }

                else -> {
                    FavoritesList(
                        favorites = uiState.favorites,
                        onFavoriteClick = onFavoriteClick,
                        onRemoveClick = onRemoveClick,
                    )
                }
            }

            // Remove confirmation dialog
            if (uiState.showRemoveDialog) {
                RemoveConfirmationDialog(
                    i18nProvider = i18nProvider,
                    favorite = uiState.favoriteToRemove!!,
                    isRemoving = uiState.isRemoving,
                    onConfirm = onConfirmRemove,
                    onDismiss = onDismissRemove,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    val i18nProvider: I18nProvider = koinInject()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.MD),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text(i18nProvider[StringKey.RETRY])
            }
        }
    }
}

@Composable
private fun EmptyState(i18nProvider: I18nProvider) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            Text(
                text = "♡",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = i18nProvider[StringKey.GuestPrompt.FAVORITES_TITLE],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = i18nProvider[StringKey.GuestPrompt.FAVORITES_MESSAGE],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Favorite>,
    onFavoriteClick: (Favorite) -> Unit,
    onRemoveClick: (Favorite) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.SM),
    ) {
        items(
            items = favorites,
            key = { it.providerId },
        ) { favorite ->
            FavoriteCard(
                favorite = favorite,
                onClick = { onFavoriteClick(favorite) },
                onRemove = { onRemoveClick(favorite) },
            )
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.XXS),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.XXS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Logo
            AsyncImage(
                model = favorite.logoUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(Spacing.XS)
                        .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(Spacing.XXS))

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
            ) {
                Text(
                    text = favorite.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Rating row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                ) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = favorite.formattedRating,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "(${favorite.reviewCountText})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                // Address row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = favorite.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Remove button
            TextButton(onClick = onRemove) {
                Text(
                    text = "❤️",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun RemoveConfirmationDialog(
    i18nProvider: I18nProvider,
    favorite: Favorite,
    isRemoving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(i18nProvider[StringKey.Provider.REMOVE_FROM_FAVORITES]) },
        text = { Text(i18nProvider[StringKey.Provider.REMOVE_CONFIRM].format(favorite.businessName)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isRemoving,
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Spacing.MD),
                        strokeWidth = Spacing.XXS,
                    )
                } else {
                    Text(i18nProvider[StringKey.DELETE])
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRemoving,
            ) {
                Text(i18nProvider[StringKey.CANCEL])
            }
        },
    )
}
