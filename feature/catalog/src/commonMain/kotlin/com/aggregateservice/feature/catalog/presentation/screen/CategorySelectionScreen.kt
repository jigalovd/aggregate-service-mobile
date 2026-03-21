package com.aggregateservice.feature.catalog.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.presentation.screenmodel.CatalogScreenModel

/**
 * Voyager Screen для выбора категории услуг.
 *
 * @property selectedCategoryId ID текущей выбранной категории (null = все)
 * @property onCategorySelected Callback при выборе категории
 */
data class CategorySelectionScreen(
    val selectedCategoryId: String? = null,
    val onCategorySelected: (Category?) -> Unit = {},
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val catalogScreenModel = koinScreenModel<CatalogScreenModel>()
        val catalogState by catalogScreenModel.uiState.collectAsState()

        CategorySelectionScreenContent(
            categories = catalogState.categories,
            selectedCategoryId = selectedCategoryId,
            isLoading = catalogState.isLoading && catalogState.categories.isEmpty(),
            onCategoryClick = { category ->
                onCategorySelected(category)
                catalogScreenModel.onCategorySelected(category)
                navigator.pop()
            },
            onBackClick = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreenContent(
    categories: List<Category>,
    selectedCategoryId: String?,
    isLoading: Boolean,
    onCategoryClick: (Category?) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выберите категорию") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (isLoading) {
            CategoryLoadingState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // "All categories" option
                CategoryItem(
                    category = null,
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategoryClick(null) },
                )

                // Category list
                LazyColumn {
                    items(
                        items = categories,
                        key = { it.id },
                    ) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { onCategoryClick(category) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category?.name ?: "Все категории",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        if (isSelected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun CategoryLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
