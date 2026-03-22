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
import androidx.compose.ui.unit.dp
import com.aggregateservice.feature.reviews.domain.usecase.CreateReviewUseCase
import com.aggregateservice.feature.reviews.presentation.model.WriteReviewUiState

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
                CheckingContent()
            }
            state.isSuccess -> {
                SuccessContent(onDismiss = onDismiss)
            }
            !state.canReview -> {
                CannotReviewContent(
                    error = state.error ?: "Вы уже оставили отзыв",
                    onDismiss = onDismiss,
                )
            }
            else -> {
                WriteReviewContent(
                    state = state,
                    onRatingChange = onRatingChange,
                    onCommentChange = onCommentChange,
                    onSubmit = onSubmit,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Проверка...",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SuccessContent(
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Спасибо за отзыв!",
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onDismiss) {
            Text("Закрыть")
        }
    }
}

@Composable
private fun CannotReviewContent(
    error: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
            Text("Закрыть")
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Оставить отзыв",
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Оценка *",
                style = MaterialTheme.typography.labelMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (rating in CreateReviewUseCase.MIN_RATING..CreateReviewUseCase.MAX_RATING) {
                    val isSelected = rating <= state.rating
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.clickable(enabled = !state.isSubmitting) {
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
            label = { Text("Комментарий (необязательно)") },
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
                Text("Отмена")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onSubmit,
                enabled = state.isValid && !state.isSubmitting,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp).height(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Отправить")
                }
            }
        }
    }
}
