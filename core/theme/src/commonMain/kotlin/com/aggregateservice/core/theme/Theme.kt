@file:Suppress("FunctionNaming", "TooManyFunctions")

package com.aggregateservice.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection

/**
 * Light color scheme for the application.
 */
private val lightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,
    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryContainer,
    onTertiaryContainer = AppColors.OnTertiaryContainer,
    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnErrorContainer,
    background = AppColors.Background,
    onBackground = AppColors.OnBackground,
    surface = AppColors.Surface,
    onSurface = AppColors.OnSurface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineVariant,
    inverseSurface = AppColors.InverseSurface,
    inverseOnSurface = AppColors.InverseOnSurface,
    inversePrimary = AppColors.InversePrimary,
    surfaceDim = AppColors.SurfaceDim,
    surfaceBright = AppColors.SurfaceBright,
    surfaceContainerLowest = AppColors.SurfaceContainerLowest,
    surfaceContainerLow = AppColors.SurfaceContainerLow,
    surfaceContainer = AppColors.SurfaceContainer,
    surfaceContainerHigh = AppColors.SurfaceContainerHigh,
    surfaceContainerHighest = AppColors.SurfaceContainerHighest,
    scrim = AppColors.Scrim,
)

/**
 * Dark color scheme for the application.
 */
private val darkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    onPrimary = AppColors.OnPrimaryDark,
    primaryContainer = AppColors.PrimaryContainerDark,
    onPrimaryContainer = AppColors.OnPrimaryContainerDark,
    secondary = AppColors.SecondaryDark,
    onSecondary = AppColors.OnSecondaryDark,
    secondaryContainer = AppColors.SecondaryContainerDark,
    onSecondaryContainer = AppColors.OnSecondaryContainerDark,
    tertiary = AppColors.TertiaryDark,
    onTertiary = AppColors.OnTertiaryDark,
    tertiaryContainer = AppColors.TertiaryContainerDark,
    onTertiaryContainer = AppColors.OnTertiaryContainerDark,
    error = AppColors.ErrorDark,
    onError = AppColors.OnErrorDark,
    errorContainer = AppColors.ErrorContainerDark,
    onErrorContainer = AppColors.OnErrorContainerDark,
    background = AppColors.BackgroundDark,
    onBackground = AppColors.OnBackgroundDark,
    surface = AppColors.SurfaceDark,
    onSurface = AppColors.OnSurfaceDark,
    surfaceVariant = AppColors.SurfaceVariantDark,
    onSurfaceVariant = AppColors.OnSurfaceVariantDark,
    outline = AppColors.OutlineDark,
    outlineVariant = AppColors.OutlineVariantDark,
    inverseSurface = AppColors.Surface,
    inverseOnSurface = AppColors.OnSurface,
    inversePrimary = AppColors.Primary,
    surfaceDim = AppColors.SurfaceDimDark,
    surfaceBright = AppColors.SurfaceBrightDark,
    surfaceContainerLowest = AppColors.SurfaceContainerLowestDark,
    surfaceContainerLow = AppColors.SurfaceContainerLowDark,
    surfaceContainer = AppColors.SurfaceContainerDark,
    surfaceContainerHigh = AppColors.SurfaceContainerHighDark,
    surfaceContainerHighest = AppColors.SurfaceContainerHighestDark,
    scrim = AppColors.Scrim,
)

/**
 * Application theme wrapper.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param layoutDirection Text direction for RTL support. Defaults to LTR.
 * @param content The content to be themed.
 */
@Composable
fun appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/**
 * Application theme with language-based RTL support.
 *
 * Automatically sets layout direction based on language code.
 * Hebrew (he) and Arabic (ar) will use RTL layout.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param languageCode The current language code (e.g., "en", "ru", "he").
 * @param content The content to be themed.
 */
@Composable
fun appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    languageCode: String,
    content: @Composable () -> Unit,
) {
    val layoutDirection = when (languageCode) {
        "he", "ar", "fa", "ur" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    appTheme(
        darkTheme = darkTheme,
        layoutDirection = layoutDirection,
        content = content,
    )
}

/**
 * Extension property to access custom colors from MaterialTheme.
 */
val MaterialTheme.appColors: AppColors
    @Composable
    get() = AppColors

/**
 * Extension property to access custom spacing from MaterialTheme.
 */
val MaterialTheme.spacing: Spacing
    @Composable
    get() = Spacing

/**
 * Extension property to access custom dimensions from MaterialTheme.
 */
val MaterialTheme.dimensions: Dimensions
    @Composable
    get() = Dimensions
