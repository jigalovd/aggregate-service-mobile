package com.aggregateservice.feature.reviews.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.reviews.domain.usecase.CreateReviewUseCase
import com.aggregateservice.feature.reviews.presentation.model.WriteReviewUiState
import org.koin.compose.koinInject

/**
 * Dialog for writing a review.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewDialog(
    state: WriteReviewUiState,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    i18nProvider: I18nProvider = koinInject(),
) {
    AlertDialog(
        onDismissRequest = {
            if (!state.isSubmitting && !state.isSuccess) {
                onDismiss()
            }
        },
        modifier = modifier,
    ) {
        when {
            state.isChecking -> {
                CheckingContent(i18nProvider = i18nProvider)
            }
            state.isSuccess -> {
                SuccessContent(onDismiss = onDismiss, i18nProvider = i18nProvider)
            }
            !state.canReview -> {
                CannotReviewContent(
                    error = state.error ?: i18nProvider[StringKey.Reviews.NO_REVIEWS],
                    onDismiss = onDismiss,
                    i18nProvider = i18nProvider,
                )
            }
            else -> {
                WriteReviewContent(
                    state = state,
                    onRatingChange = onRatingChange,
                    onCommentChange = onCommentChange,
                    onSubmit = onSubmit,
                    onDismiss = onDismiss,
                    i18nProvider = i18nProvider,
                )
            }
        }
    }
}

@Composable
private fun CheckingContent(i18nProvider: I18nProvider) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacing.XXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Spacing.MD))
        Text(
            text = i18nProvider[StringKey.LOADING],
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SuccessContent(
    onDismiss: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacing.LG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.MD),
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = i18nProvider[StringKey.SUCCESS],
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onDismiss) {
            Text(i18nProvider[StringKey.Reviews.CLOSE])
        }
    }
}

@Composable
private fun CannotReviewContent(
    error: String,
    onDismiss: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacing.LG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.MD),
    ) {
        Text(
            text = "⚠",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onDismiss) {
            Text(i18nProvider[StringKey.Reviews.CLOSE])
        }
    }
}

@Composable
private fun WriteReviewContent(
    state: WriteReviewUiState,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacing.LG),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD),
    ) {
        Text(
            text = i18nProvider[StringKey.Reviews.WRITE_REVIEW],
            style = MaterialTheme.typography.titleLarge,
        )

        if (state.providerName.isNotBlank()) {
            Text(
                text = state.providerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Rating selection
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            Text(
                text = i18nProvider[StringKey.Reviews.RATING],
                style = MaterialTheme.typography.labelMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
            ) {
                for (rating in CreateReviewUseCase.MIN_RATING..CreateReviewUseCase.MAX_RATING) {
                    val isSelected = rating <= state.rating
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.displaySmall,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        modifier =
                            Modifier.clickable(enabled = !state.isSubmitting) {
                                onRatingChange(rating)
                            },
                    )
                }
            }
        }

        // Comment field (optional)
        OutlinedTextField(
            value = state.comment,
            onValueChange = onCommentChange,
            label = { Text(i18nProvider[StringKey.Reviews.COMMENT_PLACEHOLDER]) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !state.isSubmitting,
        )

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSubmitting,
            ) {
                Text(i18nProvider[StringKey.Reviews.CANCEL])
            }
            Spacer(modifier = Modifier.width(Spacing.SM))
            TextButton(
                onClick = onSubmit,
                enabled = state.isValid && !state.isSubmitting,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(Spacing.MD).height(Spacing.MD),
                        strokeWidth = Spacing.XXS,
                    )
                } else {
                    Text(i18nProvider[StringKey.Reviews.SUBMIT])
                }
            }
        }
    }
}
