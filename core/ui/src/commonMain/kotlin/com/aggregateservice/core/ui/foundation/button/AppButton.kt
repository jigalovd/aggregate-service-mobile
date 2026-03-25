package com.aggregateservice.core.ui.foundation.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.aggregateservice.core.theme.Dimensions
import com.aggregateservice.core.theme.Spacing

/**
 * Primary button component following Material 3 design.
 *
 * **Variants:**
 * - [ButtonVariant.Primary]: Filled button for main actions
 * - [ButtonVariant.Secondary]: Outlined button for secondary actions
 * - [ButtonVariant.Tertiary]: Text-only button for tertiary actions
 * - [ButtonVariant.Danger]: Destructive actions (delete, cancel)
 *
 * **States:**
 * - Enabled, Disabled, Loading
 *
 * **Sizes:**
 * - [ButtonSize.Small]: 32dp height
 * - [ButtonSize.Medium]: 40dp height (default)
 * - [ButtonSize.Large]: 48dp height
 *
 * **Design Tokens:**
 * - Uses [Spacing] for internal padding
 * - Uses [Dimensions] for button heights
 * - Uses Material 3 typography for text
 *
 * **Accessibility:**
 * - Minimum touch target: 48dp (enforced via [Modifier.defaultMinSize])
 * - Content description for loading state
 *
 * **Usage:**
 * ```kotlin
 * AppButton(
 *     text = "Войти",
 *     onClick = { viewModel.login() },
 *     variant = ButtonVariant.Primary,
 *     size = ButtonSize.Large,
 *     isLoading = false,
 *     enabled = true,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param text The button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param variant Button visual style variant
 * @param size Button size (affects height and padding)
 * @param isLoading Shows loading indicator instead of text
 * @param enabled Whether the button is clickable
 * @param leadingIcon Optional icon at the start of the button
 * @param trailingIcon Optional icon at the end of the button
 * @param contentDescription Accessibility description for loading state
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val buttonColors = getButtonColors(variant)
    val buttonHeight = getButtonHeight(size)
    val contentPadding = getButtonPadding(size)

    val actualEnabled = enabled && !isLoading

    when (variant) {
        ButtonVariant.Primary, ButtonVariant.Danger -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = buttonHeight)
                    .semantics {
                        if (isLoading) {
                            this.contentDescription = contentDescription ?: "Loading"
                        }
                    },
                enabled = actualEnabled,
                colors = buttonColors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    enabled = actualEnabled,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    buttonColors = buttonColors,
                )
            }
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = buttonHeight)
                    .semantics {
                        if (isLoading) {
                            this.contentDescription = contentDescription ?: "Loading"
                        }
                    },
                enabled = actualEnabled,
                colors = buttonColors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    enabled = actualEnabled,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    buttonColors = buttonColors,
                )
            }
        }

        ButtonVariant.Tertiary -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = buttonHeight)
                    .semantics {
                        if (isLoading) {
                            this.contentDescription = contentDescription ?: "Loading"
                        }
                    },
                enabled = actualEnabled,
                colors = buttonColors,
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    enabled = actualEnabled,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    buttonColors = buttonColors,
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    buttonColors: ButtonColors,
) {
    val contentColor = if (enabled) {
        buttonColors.contentColor
    } else {
        buttonColors.disabledContentColor
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            leadingIcon?.invoke()

            if (leadingIcon != null) {
                Spacer(modifier = Modifier.width(Spacing.XS))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(Spacing.XS))
            }

            trailingIcon?.invoke()
        }
    }
}

@Composable
private fun getButtonColors(variant: ButtonVariant): ButtonColors {
    return when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ButtonVariant.Tertiary -> ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ButtonVariant.Danger -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun getButtonHeight(size: ButtonSize): Dp = when (size) {
    ButtonSize.Small -> Dimensions.ButtonHeightSM
    ButtonSize.Medium -> Dimensions.ButtonHeightMD
    ButtonSize.Large -> Dimensions.ButtonHeightLG
}

private fun getButtonPadding(size: ButtonSize) = when (size) {
    ButtonSize.Small -> PaddingValues(
        start = Spacing.SM,
        end = Spacing.SM,
        top = Spacing.XS,
        bottom = Spacing.XS,
    )
    ButtonSize.Medium -> ButtonDefaults.ContentPadding
    ButtonSize.Large -> PaddingValues(
        start = Spacing.LG,
        end = Spacing.LG,
        top = Spacing.SM,
        bottom = Spacing.SM,
    )
}

/**
 * Button visual style variants.
 */
enum class ButtonVariant {
    /** Filled button with primary color for main actions. */
    Primary,

    /** Outlined button for secondary actions. */
    Secondary,

    /** Text-only button for tertiary actions. */
    Tertiary,

    /** Filled button with error color for destructive actions. */
    Danger,
}

/**
 * Button size options.
 */
enum class ButtonSize {
    /** Small button (32dp height) for compact layouts. */
    Small,

    /** Medium button (40dp height) - default size. */
    Medium,

    /** Large button (48dp height) for prominent actions. */
    Large,
}

// Extension for Dp in commonMain
private val Int.dp: Dp get() = Dp(this.toFloat())
