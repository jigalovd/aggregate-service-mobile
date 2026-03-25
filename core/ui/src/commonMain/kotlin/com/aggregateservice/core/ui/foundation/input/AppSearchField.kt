package com.aggregateservice.core.ui.foundation.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Dimensions
import com.aggregateservice.core.theme.Spacing
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search

/**
 * Search input field with debounced query updates.
 *
 * Features:
 * - Debounced input to reduce unnecessary API calls
 * - Clear button (visible when text is not empty)
 * - Optional search icon
 * - Customizable debounce delay
 *
 * @param value Current search query value
 * @param onValueChange Called when the debounced value changes
 * @param modifier Modifier to be applied to the text field
 * @param placeholder Optional placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param debounceMs Debounce delay in milliseconds (default 300ms)
 * @param showSearchIcon Whether to show the search icon
 * @param onSearch Callback when search action is triggered (IME action)
 * @param colors Custom TextFieldColors
 * @param interactionSource Optional MutableInteractionSource
 */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    debounceMs: Long = 300L,
    showSearchIcon: Boolean = true,
    onSearch: (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    var currentValue by remember { mutableStateOf(value) }

    // Update internal state when external value changes
    LaunchedEffect(value) {
        if (value != currentValue) {
            currentValue = value
        }
    }

    // Debounce the value changes
    LaunchedEffect(currentValue) {
        if (currentValue != value) {
            delay(debounceMs)
            onValueChange(currentValue)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = currentValue,
        onValueChange = { currentValue = it },
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        enabled = enabled,
        readOnly = readOnly,
        leadingIcon = if (showSearchIcon) {
            {
                Icon(
                    imageVector = SearchIcon,
                    contentDescription = "Поиск",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        trailingIcon = {
            AnimatedVisibility(
                visible = currentValue.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        currentValue = ""
                        onValueChange("")
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = ClearIcon,
                        contentDescription = "Очистить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch?.invoke()
                keyboardController?.hide()
            },
            onDone = {
                keyboardController?.hide()
            }
        ),
        colors = colors,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * Compact search field variant with rounded pill shape.
 *
 * Suitable for app bars and compact spaces.
 */
@Composable
fun AppSearchFieldCompact(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    debounceMs: Long = 300L,
    showSearchIcon: Boolean = true,
    onSearch: (() -> Unit)? = null,
) {
    var currentValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            currentValue = value
        }
    }

    LaunchedEffect(currentValue) {
        if (currentValue != value) {
            delay(debounceMs)
            onValueChange(currentValue)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.MD),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSearchIcon) {
                Icon(
                    imageVector = SearchIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(Dimensions.IconSM)
                        .padding(end = Spacing.SM),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (currentValue.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Basic text input for compact variant
                // Note: For full functionality, use AppSearchField
            }

            AnimatedVisibility(
                visible = currentValue.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        currentValue = ""
                        onValueChange("")
                    },
                    modifier = Modifier.size(Dimensions.IconSM)
                ) {
                    Icon(
                        imageVector = ClearIcon,
                        contentDescription = "Очистить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Material Icons
private val SearchIcon = Icons.Filled.Search
private val ClearIcon = Icons.Filled.Close
