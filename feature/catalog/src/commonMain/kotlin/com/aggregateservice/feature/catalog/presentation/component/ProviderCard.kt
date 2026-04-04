package com.aggregateservice.feature.catalog.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.catalog.domain.model.Provider
import org.koin.compose.koinInject

/**
 * Card for displaying provider info.
 */
@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    i18nProvider: I18nProvider = koinInject(),
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MD, vertical = Spacing.SM),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MD),
        ) {
            Text(
                text = provider.businessName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(Spacing.XS))
            provider.shortDescription?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.SM))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⭐ ${provider.formattedRating}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = " • ${i18nProvider.get(StringKey.Plurals.REVIEWS_COUNT, provider.reviewCount)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = provider.location.city,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
