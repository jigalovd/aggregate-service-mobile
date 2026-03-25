package com.aggregateservice.core.ui.foundation.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import com.aggregateservice.core.theme.Dimensions
import com.aggregateservice.core.theme.Spacing

/**
 * Text input component with validation support following Material 3 design.
 *
 * **Features:**
 * - Label and placeholder text
 * - Error message display below field
 * - Leading and trailing icons
 * - Single/multi-line support
 * - Character counter (optional)
 * - Input masking support via [VisualTransformation]
 *
 * **Design Tokens:**
 * - Uses [Spacing] for padding and margins
 * - Uses [Dimensions] for field height
 * - Uses Material 3 colors and typography
 *
 * **Accessibility:**
 * - Minimum touch target: 48dp
 * - Error messages are announced by screen readers
 *
 * **Usage:**
 * ```kotlin
 * var email by remember { mutableStateOf("") }
 * var emailError by remember { mutableStateOf<String?>(null) }
 *
 * AppTextField(
 *     value = email,
 *     onValueChange = {
 *         email = it
 *         emailError = EmailValidator.validate(it).errorMessage
 *     },
 *     label = "Email",
 *     placeholder = "example@email.com",
 *     isError = emailError != null,
 *     errorMessage = emailError,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier to be applied to the text field
 * @param label Label text displayed above the field
 * @param placeholder Placeholder text when field is empty
 * @param leadingIcon Optional icon at the start of the field
 * @param trailingIcon Optional icon at the end of the field
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display below the field
 * @param enabled Whether the field is editable
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether the field is single-line only
 * @param maxLines Maximum number of lines
 * @param maxLength Maximum character length (for counter display)
 * @param showCounter Whether to show character counter
 * @param keyboardOptions Keyboard configuration options
 * @param keyboardActions Keyboard action callbacks
 * @param visualTransformation Visual transformation for input masking
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    showCounter: Boolean = maxLength != null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Dimensions.TextFieldHeight),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                errorLabelColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            shape = MaterialTheme.shapes.small,
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        // Error message or helper text
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = Spacing.MD,
                    top = Spacing.XS,
                ),
            )
        }

        // Character counter
        if (showCounter && maxLength != null) {
            Text(
                text = "${value.length}/$maxLength",
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = Spacing.MD,
                    top = Spacing.XS,
                ),
            )
        }
    }
}

/**
 * Text field with clear button.
 *
 * **Usage:**
 * ```kotlin
 * var text by remember { mutableStateOf("") }
 *
 * AppTextFieldWithClear(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "Search",
 *     onClear = { text = "" },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun AppTextFieldWithClear(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    onClear: () -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        trailingIcon = {
            if (value.isNotEmpty() && enabled) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

private val Int.dp: Dp get() = Dp(this.toFloat())
