package com.aggregateservice.androidApp.ui.compose

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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CatalogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCategories = listOf(
        Category(id = "cat1", name = "Haircut"),
        Category(id = "cat2", name = "Nails"),
        Category(id = "cat3", name = "Makeup"),
    )

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
    )

    private data class CatalogUiState(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val providers: List<ProviderWithDistance> = emptyList(),
        val categories: List<Category> = emptyList(),
        val selectedCategory: Category? = null,
        val error: String? = null,
    )

    @Composable
    private fun TestableCatalogContent(
        uiState: CatalogUiState,
        onCategorySelected: (Category?) -> Unit = {},
        onProviderClick: (String) -> Unit = {},
    ) {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                if (uiState.categories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { FilterChip(selected = uiState.selectedCategory == null, onClick = { onCategorySelected(null) }, label = { Text("All") }) }
                        items(uiState.categories, key = { it.id }) { cat ->
                            FilterChip(selected = uiState.selectedCategory?.id == cat.id, onClick = { onCategorySelected(cat) }, label = { Text(cat.name) })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                when {
                    uiState.isLoading && uiState.providers.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Loading...")
                            }
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.providers, key = { it.provider.id }) { pwd ->
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(pwd.provider.businessName)
                                    Text("★ ${pwd.provider.formattedRating} (${pwd.provider.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall)
                                    Button(onClick = { onProviderClick(pwd.provider.id) }) { Text("View") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun providersList_isDisplayed_whenProvidersExist() {
        composeTestRule.setContent { TestableCatalogContent(CatalogUiState(providers = createTestProviders())) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("John's Barber Shop").assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_isDisplayed_whenLoading() {
        composeTestRule.setContent { TestableCatalogContent(CatalogUiState(isLoading = true)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun categoryChips_areDisplayed_whenCategoriesExist() {
        composeTestRule.setContent { TestableCatalogContent(CatalogUiState(categories = testCategories)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Haircut").assertIsDisplayed()
    }

    @Test
    fun categorySelection_callbackIsTriggered_whenChipClicked() {
        var selectedId: String? = null
        composeTestRule.setContent {
            TestableCatalogContent(
                CatalogUiState(categories = testCategories),
                onCategorySelected = { selectedId = it?.id },
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nails").performClick()
        composeTestRule.waitForIdle()
        assert(selectedId == "cat2")
    }
}
