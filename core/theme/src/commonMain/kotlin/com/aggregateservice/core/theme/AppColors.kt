package com.aggregateservice.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Application color palette based on Material 3 Design System.
 *
 * Contains light and dark theme colors, semantic colors for different states,
 * and neutral colors for backgrounds and surfaces.
 */
object AppColors {
    // Primary - Main brand colors
    val Primary = Color(0xFF6750A4)
    val PrimaryContainer = Color(0xFFEADDFF)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF21005D)

    // Primary Dark theme variants
    val PrimaryDark = Color(0xFFD0BCFF)
    val PrimaryContainerDark = Color(0xFF4F378B)
    val OnPrimaryDark = Color(0xFF381E72)
    val OnPrimaryContainerDark = Color(0xFFEADDFF)

    // Secondary - Supporting accent colors
    val Secondary = Color(0xFF625B71)
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF1D192B)

    // Secondary Dark theme variants
    val SecondaryDark = Color(0xFFCCC2DC)
    val SecondaryContainerDark = Color(0xFF4A4458)
    val OnSecondaryDark = Color(0xFF332D41)
    val OnSecondaryContainerDark = Color(0xFFE8DEF8)

    // Tertiary - Additional accent
    val Tertiary = Color(0xFF7D5260)
    val TertiaryContainer = Color(0xFFFFD8E4)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF31111D)

    // Tertiary Dark theme variants
    val TertiaryDark = Color(0xFFEFB8C8)
    val TertiaryContainerDark = Color(0xFF633B48)
    val OnTertiaryDark = Color(0xFF492532)
    val OnTertiaryContainerDark = Color(0xFFFFD8E4)

    // Error - For validation errors and destructive actions
    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF410E0B)

    // Error Dark theme variants
    val ErrorDark = Color(0xFFF2B8B5)
    val ErrorContainerDark = Color(0xFF8C1D18)
    val OnErrorDark = Color(0xFF601410)
    val OnErrorContainerDark = Color(0xFFF9DEDC)

    // Semantic colors - Application-specific states
    val Success = Color(0xFF4CAF50)
    val SuccessContainer = Color(0xFFE8F5E9)
    val OnSuccess = Color(0xFFFFFFFF)
    val OnSuccessContainer = Color(0xFF1B5E20)

    val Warning = Color(0xFFFF9800)
    val WarningContainer = Color(0xFFFFF3E0)
    val OnWarning = Color(0xFFFFFFFF)
    val OnWarningContainer = Color(0xFFE65100)

    val Info = Color(0xFF2196F3)
    val InfoContainer = Color(0xFFE3F2FD)
    val OnInfo = Color(0xFFFFFFFF)
    val OnInfoContainer = Color(0xFF0D47A1)

    // Neutral - Backgrounds and surfaces (Light theme)
    val Surface = Color(0xFFFFFBFE)
    val SurfaceDim = Color(0xFFDED8E1)
    val SurfaceBright = Color(0xFFFFFBFE)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF7F2FA)
    val SurfaceContainer = Color(0xFFF3EDF7)
    val SurfaceContainerHigh = Color(0xFFECE6F0)
    val SurfaceContainerHighest = Color(0xFFE6E0E9)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val Background = Color(0xFFFFFBFE)

    // Neutral Dark theme variants
    val SurfaceDark = Color(0xFF1C1B1F)
    val SurfaceDimDark = Color(0xFF141218)
    val SurfaceBrightDark = Color(0xFF3B383E)
    val SurfaceContainerLowestDark = Color(0xFF0F0D13)
    val SurfaceContainerLowDark = Color(0xFF1D1B20)
    val SurfaceContainerDark = Color(0xFF211F26)
    val SurfaceContainerHighDark = Color(0xFF2B2930)
    val SurfaceContainerHighestDark = Color(0xFF36343B)
    val SurfaceVariantDark = Color(0xFF49454F)
    val BackgroundDark = Color(0xFF1C1B1F)

    // Outlines
    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)
    val OutlineDark = Color(0xFF938F99)
    val OutlineVariantDark = Color(0xFF49454F)

    // Text colors (Light theme)
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF49454F)
    val OnBackground = Color(0xFF1C1B1F)

    // Text colors (Dark theme)
    val OnSurfaceDark = Color(0xFFE6E1E5)
    val OnSurfaceVariantDark = Color(0xFFCAC4D0)
    val OnBackgroundDark = Color(0xFFE6E1E5)

    // Inverse colors
    val InverseSurface = Color(0xFF313033)
    val InverseOnSurface = Color(0xFFF4EFF4)
    val InversePrimary = Color(0xFFD0BCFF)

    // Scrim
    val Scrim = Color(0xFF000000)

    // Shadow
    val Shadow = Color(0xFF000000)
}
