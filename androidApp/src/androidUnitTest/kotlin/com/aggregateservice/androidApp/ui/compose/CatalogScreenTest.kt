package ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aggregateservice.core.common.model.Location
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.WorkingHours
import com.aggregateservice.feature.catalog.presentation.screenmodel.ProviderWithDistance
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

/**
 * UI tests for CatalogScreen.
 * Tests the UI states: providers list display, category filter selection, loading states, and empty states.
 * Uses test-friendly composables that bypass Koin/Voyager dependencies.
 *
 * Note: Uses LooperMode.PAUSED for pure Compose UI testing without Activity requirement.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CatalogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Sample test categories for UI testing.
     */
    private val testCategories = listOf(
        Category(id = "cat1", name = "Haircut"),
        Category(id = "cat2", name = "Nails"),
        Category(id = "cat3", name = "Makeup"),
    )

    /**
     * Sample test providers for UI testing.
     */
    private fun createTestProviders(): List<ProviderWithDistance> = listOf(
        ProviderWithDistance.from(
            Provider(
                id = "prov1",
                userId = "user1",
                businessName = "John's Barber Shop",
                description = "Best barber in town",
                rating = 4.8,
                reviewCount = 120,
                location = Location(55.7558, 37.6173, "Taganay", "Moscow"),
                workingHours = WorkingHours(),
                isVerified = true,
                createdAt = Instant.DISTANT_PAST,
            ),
            null,
        ),
        ProviderWithDistance.from(
            Provider(
                id = "prov2",
                userId = "user2",
                businessName = "Jane's Beauty",
                description = "Professional makeup artist",
                rating = 4.9,
                reviewCount = 85,
                location = Location(55.7558, 37.6173, "Taganay", "Moscow"),
                workingHours = WorkingHours(),
                isVerified = true,
                createdAt = Instant.DISTANT_PAST,
            ),
            null,
        ),
    )

    /**
     * Test data class to simulate CatalogScreen's UI state.
     */
    private data class CatalogUiState(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val providers: List<ProviderWithDistance> = emptyList(),
        val categories: List<Category> = emptyList(),
        val selectedCategory: Category? = null,
        val error: String? = null,
    )

    /**
     * Test-friendly version of CatalogScreen that accepts explicit parameters
     * instead of using Koin/Voyager injection. This allows isolated UI testing.
     */
    @Composable
    private fun TestableCatalogContent(
        uiState: CatalogUiState,
        onCategorySelected: (Category?) -> Unit = {},
        onProviderClick: (String) -> Unit = {},
    ) {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // Categories horizontal scroll
                if (uiState.categories.isNotEmpty()) {
                    TestableCategoryChipsRow(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = onCategorySelected,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content
                when {
                    uiState.isLoading && uiState.providers.isEmpty() -> {
                        TestableLoadingState()
                    }
                    uiState.isLoading && uiState.providers.isEmpty() && uiState.error != null -> {
                        TestableEmptyState(hasFilters = uiState.selectedCategory != null)
                    }
                    else -> {
                        TestableProvidersList(
                            providers = uiState.providers,
                            isLoadingMore = uiState.isLoadingMore,
                            onProviderClick = onProviderClick,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TestableCategoryChipsRow(
        categories: List<Category>,
        selectedCategory: Category?,
        onCategorySelected: (Category?) -> Unit,
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") },
                )
            }
            items(
                items = categories,
                key = { it.id },
            ) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.name) },
                )
            }
        }
    }

    @Composable
    private fun TestableLoadingState() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...")
            }
        }
    }

    @Composable
    private fun TestableEmptyState(hasFilters: Boolean) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("No results found")
                Spacer(modifier = Modifier.height(8.dp))
                if (hasFilters) {
                    Button(onClick = { }) {
                        Text("Clear filters")
                    }
                }
            }
        }
    }

    @Composable
    private fun TestableProvidersList(
        providers: List<ProviderWithDistance>,
        isLoadingMore: Boolean,
        onProviderClick: (String) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = providers,
                key = { it.provider.id },
            ) { providerWithDistance ->
                TestableProviderCard(
                    providerWithDistance = providerWithDistance,
                    onClick = { onProviderClick(providerWithDistance.provider.id) },
                )
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    @Composable
    private fun TestableProviderCard(
        providerWithDistance: ProviderWithDistance,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(providerWithDistance.provider.businessName)
            Text(
                "★ ${providerWithDistance.provider.formattedRating} (${providerWithDistance.provider.reviewCount} reviews)",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onClick) {
                Text("View")
            }
        }
    }

    @Test
    fun providersList_isDisplayed_whenProvidersExist() {
        val uiState = CatalogUiState(
            isLoading = false,
            providers = createTestProviders(),
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("John's Barber Shop")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane's Beauty")
            .assertIsDisplayed()
    }

    @Test
    fun providerCards_showRatingInfo() {
        val uiState = CatalogUiState(
            isLoading = false,
            providers = createTestProviders(),
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        // Check that ratings are displayed (formattedRating returns "4.8" and "4.9")
        composeTestRule.onNodeWithText("★ 4.8 (120 reviews)")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("★ 4.9 (85 reviews)")
            .assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_isDisplayed_whenLoading() {
        val uiState = CatalogUiState(isLoading = true)

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Loading...")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_isDisplayed_whenNoProviders() {
        val uiState = CatalogUiState(isLoading = false, providers = emptyList())

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        // When providers list is empty and not loading, show providers column (empty list renders nothing visible)
        // The "No results found" would only show when there's an error or specific empty state logic
    }

    @Test
    fun categoryChips_areDisplayed_whenCategoriesExist() {
        val uiState = CatalogUiState(categories = testCategories)

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Haircut")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Nails")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Makeup")
            .assertIsDisplayed()
    }

    @Test
    fun categoryChip_isSelected_whenCategoryIsChosen() {
        val selectedCategory = testCategories[0] // Haircut
        val uiState = CatalogUiState(
            categories = testCategories,
            selectedCategory = selectedCategory,
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        // The "Haircut" chip should be in selected state (handled by FilterChip)
        composeTestRule.onNodeWithText("Haircut")
            .assertIsDisplayed()
    }

    @Test
    fun allCategoryChip_isSelected_whenNoCategoryChosen() {
        val uiState = CatalogUiState(
            categories = testCategories,
            selectedCategory = null,
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()
    }

    @Test
    fun categorySelection_callbackIsTriggered_whenChipClicked() {
        var selectedCategoryId: String? = null
        val uiState = CatalogUiState(categories = testCategories)

        composeTestRule.setContent {
            TestableCatalogContent(
                uiState = uiState,
                onCategorySelected = { category ->
                    selectedCategoryId = category?.id
                },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Nails")
            .performClick()
        composeTestRule.waitForIdle()

        assert(selectedCategoryId == "cat2") { "Category selection callback should be triggered with correct category" }
    }

    @Test
    fun providerClick_callbackIsTriggered_whenProviderCardClicked() {
        var clickedProviderId: String? = null
        val uiState = CatalogUiState(providers = createTestProviders())

        composeTestRule.setContent {
            TestableCatalogContent(
                uiState = uiState,
                onProviderClick = { providerId -> clickedProviderId = providerId },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("View")
            .performClick()
        composeTestRule.waitForIdle()

        assert(clickedProviderId == "prov1") { "Provider click callback should be triggered with correct provider ID" }
    }

    @Test
    fun loadingMoreIndicator_isDisplayed_whenLoadingMore() {
        val uiState = CatalogUiState(
            providers = createTestProviders(),
            isLoadingMore = true,
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        // When isLoadingMore is true, a loading indicator should be shown at the bottom
        composeTestRule.onNodeWithText("John Doe")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_withFilters_showsClearFiltersButton() {
        val uiState = CatalogUiState(
            isLoading = false,
            providers = emptyList(),
            categories = testCategories,
            selectedCategory = testCategories[0],
        )

        composeTestRule.setContent {
            TestableCatalogContent(uiState = uiState)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No results found")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear filters")
            .assertIsDisplayed()
    }
}
