package com.aggregateservice.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [AppColors] color palette.
 * Verifies that color values are correctly defined for both light and dark themes.
 */
class AppColorsTest {

    // region Primary Colors Tests

    @Test
    fun `Primary color has correct value`() {
        assertEquals(Color(0xFF6750A4), AppColors.Primary)
    }

    @Test
    fun `PrimaryContainer color has correct value`() {
        assertEquals(Color(0xFFEADDFF), AppColors.PrimaryContainer)
    }

    @Test
    fun `OnPrimary color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnPrimary)
    }

    @Test
    fun `OnPrimaryContainer color has correct value`() {
        assertEquals(Color(0xFF21005D), AppColors.OnPrimaryContainer)
    }

    // region Primary Dark Colors Tests

    @Test
    fun `PrimaryDark color has correct value`() {
        assertEquals(Color(0xFFD0BCFF), AppColors.PrimaryDark)
    }

    @Test
    fun `PrimaryContainerDark color has correct value`() {
        assertEquals(Color(0xFF4F378B), AppColors.PrimaryContainerDark)
    }

    // endregion

    // endregion

    // region Secondary Colors Tests

    @Test
    fun `Secondary color has correct value`() {
        assertEquals(Color(0xFF625B71), AppColors.Secondary)
    }

    @Test
    fun `SecondaryContainer color has correct value`() {
        assertEquals(Color(0xFFE8DEF8), AppColors.SecondaryContainer)
    }

    @Test
    fun `OnSecondary color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnSecondary)
    }

    @Test
    fun `OnSecondaryContainer color has correct value`() {
        assertEquals(Color(0xFF1D192B), AppColors.OnSecondaryContainer)
    }

    // region Secondary Dark Colors Tests

    @Test
    fun `SecondaryDark color has correct value`() {
        assertEquals(Color(0xFFCCC2DC), AppColors.SecondaryDark)
    }

    @Test
    fun `SecondaryContainerDark color has correct value`() {
        assertEquals(Color(0xFF4A4458), AppColors.SecondaryContainerDark)
    }

    // endregion

    // endregion

    // region Tertiary Colors Tests

    @Test
    fun `Tertiary color has correct value`() {
        assertEquals(Color(0xFF7D5260), AppColors.Tertiary)
    }

    @Test
    fun `TertiaryContainer color has correct value`() {
        assertEquals(Color(0xFFFFD8E4), AppColors.TertiaryContainer)
    }

    @Test
    fun `OnTertiary color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnTertiary)
    }

    @Test
    fun `OnTertiaryContainer color has correct value`() {
        assertEquals(Color(0xFF31111D), AppColors.OnTertiaryContainer)
    }

    // region Tertiary Dark Colors Tests

    @Test
    fun `TertiaryDark color has correct value`() {
        assertEquals(Color(0xFFEFB8C8), AppColors.TertiaryDark)
    }

    @Test
    fun `TertiaryContainerDark color has correct value`() {
        assertEquals(Color(0xFF633B48), AppColors.TertiaryContainerDark)
    }

    // endregion

    // endregion

    // region Error Colors Tests

    @Test
    fun `Error color has correct value`() {
        assertEquals(Color(0xFFB3261E), AppColors.Error)
    }

    @Test
    fun `ErrorContainer color has correct value`() {
        assertEquals(Color(0xFFF9DEDC), AppColors.ErrorContainer)
    }

    @Test
    fun `OnError color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnError)
    }

    @Test
    fun `OnErrorContainer color has correct value`() {
        assertEquals(Color(0xFF410E0B), AppColors.OnErrorContainer)
    }

    // region Error Dark Colors Tests

    @Test
    fun `ErrorDark color has correct value`() {
        assertEquals(Color(0xFFF2B8B5), AppColors.ErrorDark)
    }

    @Test
    fun `ErrorContainerDark color has correct value`() {
        assertEquals(Color(0xFF8C1D18), AppColors.ErrorContainerDark)
    }

    // endregion

    // endregion

    // region Semantic Colors Tests

    @Test
    fun `Success color has correct value`() {
        assertEquals(Color(0xFF4CAF50), AppColors.Success)
    }

    @Test
    fun `SuccessContainer color has correct value`() {
        assertEquals(Color(0xFFE8F5E9), AppColors.SuccessContainer)
    }

    @Test
    fun `OnSuccess color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnSuccess)
    }

    @Test
    fun `OnSuccessContainer color has correct value`() {
        assertEquals(Color(0xFF1B5E20), AppColors.OnSuccessContainer)
    }

    @Test
    fun `Warning color has correct value`() {
        assertEquals(Color(0xFFFF9800), AppColors.Warning)
    }

    @Test
    fun `WarningContainer color has correct value`() {
        assertEquals(Color(0xFFFFF3E0), AppColors.WarningContainer)
    }

    @Test
    fun `OnWarning color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnWarning)
    }

    @Test
    fun `OnWarningContainer color has correct value`() {
        assertEquals(Color(0xFFE65100), AppColors.OnWarningContainer)
    }

    @Test
    fun `Info color has correct value`() {
        assertEquals(Color(0xFF2196F3), AppColors.Info)
    }

    @Test
    fun `InfoContainer color has correct value`() {
        assertEquals(Color(0xFFE3F2FD), AppColors.InfoContainer)
    }

    @Test
    fun `OnInfo color has correct value`() {
        assertEquals(Color(0xFFFFFFFF), AppColors.OnInfo)
    }

    @Test
    fun `OnInfoContainer color has correct value`() {
        assertEquals(Color(0xFF0D47A1), AppColors.OnInfoContainer)
    }

    // endregion

    // region Surface Colors Tests (Light Theme)

    @Test
    fun `Surface color has correct value`() {
        assertEquals(Color(0xFFFFFBFE), AppColors.Surface)
    }

    @Test
    fun `SurfaceDim color has correct value`() {
        assertEquals(Color(0xFFDED8E1), AppColors.SurfaceDim)
    }

    @Test
    fun `SurfaceVariant color has correct value`() {
        assertEquals(Color(0xFFE7E0EC), AppColors.SurfaceVariant)
    }

    @Test
    fun `Background color has correct value`() {
        assertEquals(Color(0xFFFFFBFE), AppColors.Background)
    }

    // endregion

    // region Surface Colors Tests (Dark Theme)

    @Test
    fun `SurfaceDark color has correct value`() {
        assertEquals(Color(0xFF1C1B1F), AppColors.SurfaceDark)
    }

    @Test
    fun `SurfaceDimDark color has correct value`() {
        assertEquals(Color(0xFF141218), AppColors.SurfaceDimDark)
    }

    @Test
    fun `SurfaceVariantDark color has correct value`() {
        assertEquals(Color(0xFF49454F), AppColors.SurfaceVariantDark)
    }

    @Test
    fun `BackgroundDark color has correct value`() {
        assertEquals(Color(0xFF1C1B1F), AppColors.BackgroundDark)
    }

    // endregion

    // region Outline Colors Tests

    @Test
    fun `Outline color has correct value`() {
        assertEquals(Color(0xFF79747E), AppColors.Outline)
    }

    @Test
    fun `OutlineVariant color has correct value`() {
        assertEquals(Color(0xFFCAC4D0), AppColors.OutlineVariant)
    }

    @Test
    fun `OutlineDark color has correct value`() {
        assertEquals(Color(0xFF938F99), AppColors.OutlineDark)
    }

    @Test
    fun `OutlineVariantDark color has correct value`() {
        assertEquals(Color(0xFF49454F), AppColors.OutlineVariantDark)
    }

    // endregion

    // region OnSurface Colors Tests

    @Test
    fun `OnSurface color has correct value`() {
        assertEquals(Color(0xFF1C1B1F), AppColors.OnSurface)
    }

    @Test
    fun `OnSurfaceVariant color has correct value`() {
        assertEquals(Color(0xFF49454F), AppColors.OnSurfaceVariant)
    }

    @Test
    fun `OnBackground color has correct value`() {
        assertEquals(Color(0xFF1C1B1F), AppColors.OnBackground)
    }

    @Test
    fun `OnSurfaceDark color has correct value`() {
        assertEquals(Color(0xFFE6E1E5), AppColors.OnSurfaceDark)
    }

    @Test
    fun `OnSurfaceVariantDark color has correct value`() {
        assertEquals(Color(0xFFCAC4D0), AppColors.OnSurfaceVariantDark)
    }

    @Test
    fun `OnBackgroundDark color has correct value`() {
        assertEquals(Color(0xFFE6E1E5), AppColors.OnBackgroundDark)
    }

    // endregion

    // region Inverse Colors Tests

    @Test
    fun `InverseSurface color has correct value`() {
        assertEquals(Color(0xFF313033), AppColors.InverseSurface)
    }

    @Test
    fun `InverseOnSurface color has correct value`() {
        assertEquals(Color(0xFFF4EFF4), AppColors.InverseOnSurface)
    }

    @Test
    fun `InversePrimary color has correct value`() {
        assertEquals(Color(0xFFD0BCFF), AppColors.InversePrimary)
    }

    // endregion

    // region Scrim and Shadow Tests

    @Test
    fun `Scrim color has correct value`() {
        assertEquals(Color(0xFF000000), AppColors.Scrim)
    }

    @Test
    fun `Shadow color has correct value`() {
        assertEquals(Color(0xFF000000), AppColors.Shadow)
    }

    // endregion
}
