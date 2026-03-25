package com.aggregateservice.core.ui.foundation.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.aggregateservice.core.theme.Dimensions
import com.aggregateservice.core.theme.Spacing

/**
 * Password input with visibility toggle following Material 3 design.
 *
 * **Features:**
 * - All [AppTextField] features
 * - Automatic visibility toggle icon
 * - Optional password strength indicator
 *
 * **Design Tokens:**
 * - Uses [Spacing] for padding
 * - Uses [Dimensions] for component heights
 * - Uses Material 3 colors for strength indicator
 *
 * **Usage:**
 * ```kotlin
 * var password by remember { mutableStateOf("") }
 * var passwordError by remember { mutableStateOf<String?>(null) }
 *
 * AppPasswordField(
 *     value = password,
 *     onValueChange = {
 *         password = it
 *         passwordError = validatePassword(it)
 *     },
 *     label = "Пароль",
 *     isError = passwordError != null,
 *     errorMessage = passwordError,
 *     showStrengthIndicator = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param value Current password value
 * @param onValueChange Callback when password changes
 * @param modifier Modifier to be applied to the field
 * @param label Label text displayed above the field
 * @param placeholder Placeholder text when field is empty
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display below the field
 * @param enabled Whether the field is editable
 * @param showStrengthIndicator Whether to show password strength meter
 * @param showVisibilityToggle Whether to show visibility toggle button
 * @param imeAction IME action for keyboard (default: Done)
 * @param onImeAction Callback when IME action is triggered
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun AppPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    showStrengthIndicator: Boolean = false,
    showVisibilityToggle: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = if (showVisibilityToggle) {
                {
                    val icon = if (passwordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    }
                    val description = if (passwordVisible) {
                        "Скрыть пароль"
                    } else {
                        "Показать пароль"
                    }

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = description,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onGo = { onImeAction() },
                onSearch = { onImeAction() },
                onSend = { onImeAction() },
            ),
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

        // Password strength indicator
        if (showStrengthIndicator && value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.SM))
            PasswordStrengthIndicator(
                password = value,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Error message
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
    }
}

/**
 * Password strength indicator with visual bar and label.
 *
 * **Strength Levels:**
 * - Weak (0-2): Red, < 8 characters or simple patterns
 * - Medium (3): Yellow, 8+ characters with mixed types
 * - Strong (4): Green, 12+ characters with all character types
 *
 * @param password Current password to evaluate
 * @param modifier Modifier to be applied
 */
@Composable
private fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
) {
    val strength = calculatePasswordStrength(password)
    val (color, label) = getStrengthColorAndLabel(strength)

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { strength / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.DividerThickness * 2),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Text(
            text = "Надёжность: $label",
            color = color,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.XS),
        )
    }
}

/**
 * Calculates password strength on a scale of 0-4.
 *
 * **Criteria:**
 * 1. Length >= 8: +1
 * 2. Contains lowercase: +1
 * 3. Contains uppercase: +1
 * 4. Contains digit: +1
 * 5. Contains special character: +1
 *
 * @param password Password to evaluate
 * @return Strength score (0-4)
 */
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0

    // Length check
    if (password.length >= 8) strength++
    if (password.length >= 12) strength++

    // Character type checks
    if (password.any { it.isLowerCase() }) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++

    // Cap at 4
    return strength.coerceIn(0, 4)
}

@Composable
private fun getStrengthColorAndLabel(strength: Int): Pair<androidx.compose.ui.graphics.Color, String> {
    return when (strength) {
        0, 1 -> MaterialTheme.colorScheme.error to "Слабый"
        2 -> androidx.compose.ui.graphics.Color(0xFFFF9800) to "Средний" // Warning color
        3 -> androidx.compose.ui.graphics.Color(0xFF8BC34A) to "Хороший"
        4 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) to "Надёжный"
        else -> MaterialTheme.colorScheme.error to "Слабый"
    }
}
