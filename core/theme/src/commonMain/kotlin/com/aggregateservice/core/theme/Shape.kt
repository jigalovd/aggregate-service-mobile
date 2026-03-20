package com.aggregateservice.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Application shapes based on Material 3 Design System.
 *
 * Defines corner radii for different component categories.
 */
val AppShapes = Shapes(
    // Extra Small - Chips, small buttons
    extraSmall = RoundedCornerShape(4.dp),
    // Small - Text fields, cards (small)
    small = RoundedCornerShape(8.dp),
    // Medium - Cards, dialogs
    medium = RoundedCornerShape(12.dp),
    // Large - Bottom sheets, large cards
    large = RoundedCornerShape(16.dp),
    // Extra Large - Modal bottom sheets, navigation drawers
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Additional shape definitions for specific use cases.
 */
object AppShape {
    // Full rounded shape for avatars, FAB
    val Full = RoundedCornerShape(50)

    // No rounding
    val None = RoundedCornerShape(0.dp)

    // Card shapes
    val CardSmall = RoundedCornerShape(8.dp)
    val CardMedium = RoundedCornerShape(12.dp)
    val CardLarge = RoundedCornerShape(16.dp)

    // Button shapes
    val Button = RoundedCornerShape(20.dp)
    val ButtonSmall = RoundedCornerShape(12.dp)

    // Input field shapes
    val TextField = RoundedCornerShape(4.dp)

    // Bottom sheet shapes
    val BottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    // Dialog shape
    val Dialog = RoundedCornerShape(28.dp)
}
