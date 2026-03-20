package com.aggregateservice.core.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Application dimensions and spacing tokens.
 *
 * Follows an 8dp grid system for consistent spacing throughout the app.
 */
object Spacing {
    // Base unit
    val None = 0.dp

    // Extra Extra Small - Minimal spacing
    val XXS = 2.dp

    // Extra Small - Icon padding, badges
    val XS = 4.dp

    // Small - Internal component padding
    val SM = 8.dp

    // Medium - Standard padding between elements
    val MD = 16.dp

    // Large - Section spacing
    val LG = 24.dp

    // Extra Large - Screen section spacing
    val XL = 32.dp

    // Extra Extra Large - Large block spacing
    val XXL = 48.dp

    // Extra Extra Extra Large - Hero section spacing
    val XXXL = 64.dp
}

/**
 * Component-specific dimensions.
 */
object Dimensions {
    // Icon sizes
    val IconXS = 16.dp
    val IconSM = 20.dp
    val IconMD = 24.dp
    val IconLG = 32.dp
    val IconXL = 48.dp

    // Button heights
    val ButtonHeightSM = 32.dp
    val ButtonHeightMD = 40.dp
    val ButtonHeightLG = 48.dp
    val ButtonHeightXL = 56.dp

    // Input field heights
    val TextFieldHeight = 56.dp
    val TextFieldHeightSM = 48.dp

    // Card dimensions
    val CardElevation = 2.dp
    val CardElevationPressed = 8.dp
    val CardCornerSize = 12.dp

    // List item heights
    val ListItemHeightSM = 48.dp
    val ListItemHeightMD = 56.dp
    val ListItemHeightLG = 72.dp
    val ListItemHeightXL = 88.dp

    // Avatar sizes
    val AvatarSM = 32.dp
    val AvatarMD = 40.dp
    val AvatarLG = 56.dp
    val AvatarXL = 80.dp

    // Thumbnail sizes
    val ThumbnailSM = 48.dp
    val ThumbnailMD = 64.dp
    val ThumbnailLG = 96.dp
    val ThumbnailXL = 128.dp

    // Divider
    val DividerThickness = 1.dp

    // Border
    val BorderWidth = 1.dp
    val BorderWidthThick = 2.dp

    // Minimum touch target (accessibility)
    val MinTouchTarget = 48.dp

    // Screen padding
    val ScreenPaddingHorizontal = 16.dp
    val ScreenPaddingTop = 0.dp
    val ScreenPaddingBottom = 16.dp

    // Bottom navigation
    val BottomNavHeight = 80.dp

    // Top app bar
    val TopAppBarHeight = 64.dp

    // FAB
    val FabSize = 56.dp
    val FabSizeMini = 40.dp
}

/**
 * Animation durations in milliseconds.
 */
object Animation {
    const val Fast = 100
    const val Normal = 200
    const val Slow = 400
    const val VerySlow = 800
}

/**
 * Font size constants for direct use when typography isn't appropriate.
 */
object FontSize {
    val XS = 10.sp
    val SM = 12.sp
    val MD = 14.sp
    val LG = 16.sp
    val XL = 18.sp
    val XXL = 20.sp
    val XXXL = 24.sp
    val Display = 32.sp
}
