package com.aggregateservice.core.ui.foundation.selection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing

/**
 * Checkbox component with optional label and support text.
 *
 * Features:
 * - Label positioning (start or end)
 * - Support/error text
 * - Enabled/disabled states
 * - Clickable entire row
 *
 * @param checked Current checked state
 * @param onCheckedChange Callback when checked state changes
 * @param modifier Modifier to be applied to the checkbox row
 * @param label Optional label text
 * @param labelAsAnnotatedString Optional label as AnnotatedString for rich text
 * @param supportText Optional support text below the label
 * @param enabled Whether the checkbox is enabled
 * @param labelPosition Position of the label relative to checkbox
 * @param colors Custom CheckboxColors
 * @param interactionSource Optional MutableInteractionSource
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    labelAsAnnotatedString: AnnotatedString? = null,
    supportText: String? = null,
    enabled: Boolean = true,
    labelPosition: LabelPosition = LabelPosition.End,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { role = Role.Checkbox },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (labelPosition == LabelPosition.Start && (label != null || labelAsAnnotatedString != null)) {
            LabelContent(
                label = label,
                labelAsAnnotatedString = labelAsAnnotatedString,
                supportText = supportText,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(Spacing.MD))
        }

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
            modifier = Modifier.semantics { role = Role.Checkbox }
        )

        if (labelPosition == LabelPosition.End && (label != null || labelAsAnnotatedString != null)) {
            Spacer(modifier = Modifier.width(Spacing.MD))
            LabelContent(
                label = label,
                labelAsAnnotatedString = labelAsAnnotatedString,
                supportText = supportText,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Tri-state checkbox with optional label.
 *
 * Supports three states: Checked, Unchecked, Indeterminate.
 * Useful for parent checkboxes that control child items.
 *
 * @param state Current toggleable state
 * @param onClick Callback when checkbox is clicked
 * @param modifier Modifier to be applied to the checkbox row
 * @param label Optional label text
 * @param enabled Whether the checkbox is enabled
 * @param labelPosition Position of the label relative to checkbox
 * @param colors Custom CheckboxColors
 * @param interactionSource Optional MutableInteractionSource
 */
@Composable
fun AppTriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    labelPosition: LabelPosition = LabelPosition.End,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { role = Role.Checkbox },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (labelPosition == LabelPosition.Start && label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(Spacing.MD))
        }

        TriStateCheckbox(
            state = state,
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource
        )

        if (labelPosition == LabelPosition.End && label != null) {
            Spacer(modifier = Modifier.width(Spacing.MD))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Position of the label relative to the checkbox.
 */
enum class LabelPosition {
    Start,
    End
}

@Composable
private fun LabelContent(
    label: String?,
    labelAsAnnotatedString: AnnotatedString?,
    supportText: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    androidx.compose.foundation.layout.Column(
        modifier = modifier
    ) {
        when {
            labelAsAnnotatedString != null -> {
                Text(
                    text = labelAsAnnotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            label != null -> {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (supportText != null) {
            Text(
                text = supportText,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
