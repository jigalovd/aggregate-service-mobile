package com.aggregateservice.core.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [Spacing] dimension tokens.
 * Verifies that spacing values follow the 8dp grid system.
 */
class SpacingTest {

    @Test
    fun `None has correct value`() {
        assertEquals(0.dp, Spacing.None)
    }

    @Test
    fun `XXS has correct value`() {
        assertEquals(2.dp, Spacing.XXS)
    }

    @Test
    fun `XS has correct value`() {
        assertEquals(4.dp, Spacing.XS)
    }

    @Test
    fun `SM has correct value`() {
        assertEquals(8.dp, Spacing.SM)
    }

    @Test
    fun `MD has correct value`() {
        assertEquals(16.dp, Spacing.MD)
    }

    @Test
    fun `LG has correct value`() {
        assertEquals(24.dp, Spacing.LG)
    }

    @Test
    fun `XL has correct value`() {
        assertEquals(32.dp, Spacing.XL)
    }

    @Test
    fun `XXL has correct value`() {
        assertEquals(48.dp, Spacing.XXL)
    }

    @Test
    fun `XXXL has correct value`() {
        assertEquals(64.dp, Spacing.XXXL)
    }
}

/**
 * Tests for [Dimensions] component tokens.
 * Verifies that component-specific dimension values are correct.
 */
class DimensionsTest {

    // region Icon Sizes Tests

    @Test
    fun `IconXS has correct value`() {
        assertEquals(16.dp, Dimensions.IconXS)
    }

    @Test
    fun `IconSM has correct value`() {
        assertEquals(20.dp, Dimensions.IconSM)
    }

    @Test
    fun `IconMD has correct value`() {
        assertEquals(24.dp, Dimensions.IconMD)
    }

    @Test
    fun `IconLG has correct value`() {
        assertEquals(32.dp, Dimensions.IconLG)
    }

    @Test
    fun `IconXL has correct value`() {
        assertEquals(48.dp, Dimensions.IconXL)
    }

    // endregion

    // region Button Heights Tests

    @Test
    fun `ButtonHeightSM has correct value`() {
        assertEquals(32.dp, Dimensions.ButtonHeightSM)
    }

    @Test
    fun `ButtonHeightMD has correct value`() {
        assertEquals(40.dp, Dimensions.ButtonHeightMD)
    }

    @Test
    fun `ButtonHeightLG has correct value`() {
        assertEquals(48.dp, Dimensions.ButtonHeightLG)
    }

    @Test
    fun `ButtonHeightXL has correct value`() {
        assertEquals(56.dp, Dimensions.ButtonHeightXL)
    }

    // endregion

    // region Input Field Heights Tests

    @Test
    fun `TextFieldHeight has correct value`() {
        assertEquals(56.dp, Dimensions.TextFieldHeight)
    }

    @Test
    fun `TextFieldHeightSM has correct value`() {
        assertEquals(48.dp, Dimensions.TextFieldHeightSM)
    }

    // endregion

    // region Card Dimensions Tests

    @Test
    fun `CardElevation has correct value`() {
        assertEquals(2.dp, Dimensions.CardElevation)
    }

    @Test
    fun `CardElevationPressed has correct value`() {
        assertEquals(8.dp, Dimensions.CardElevationPressed)
    }

    @Test
    fun `CardCornerSize has correct value`() {
        assertEquals(12.dp, Dimensions.CardCornerSize)
    }

    // endregion

    // region List Item Heights Tests

    @Test
    fun `ListItemHeightSM has correct value`() {
        assertEquals(48.dp, Dimensions.ListItemHeightSM)
    }

    @Test
    fun `ListItemHeightMD has correct value`() {
        assertEquals(56.dp, Dimensions.ListItemHeightMD)
    }

    @Test
    fun `ListItemHeightLG has correct value`() {
        assertEquals(72.dp, Dimensions.ListItemHeightLG)
    }

    @Test
    fun `ListItemHeightXL has correct value`() {
        assertEquals(88.dp, Dimensions.ListItemHeightXL)
    }

    // endregion

    // region Avatar Sizes Tests

    @Test
    fun `AvatarSM has correct value`() {
        assertEquals(32.dp, Dimensions.AvatarSM)
    }

    @Test
    fun `AvatarMD has correct value`() {
        assertEquals(40.dp, Dimensions.AvatarMD)
    }

    @Test
    fun `AvatarLG has correct value`() {
        assertEquals(56.dp, Dimensions.AvatarLG)
    }

    @Test
    fun `AvatarXL has correct value`() {
        assertEquals(80.dp, Dimensions.AvatarXL)
    }

    // endregion

    // region Thumbnail Sizes Tests

    @Test
    fun `ThumbnailSM has correct value`() {
        assertEquals(48.dp, Dimensions.ThumbnailSM)
    }

    @Test
    fun `ThumbnailMD has correct value`() {
        assertEquals(64.dp, Dimensions.ThumbnailMD)
    }

    @Test
    fun `ThumbnailLG has correct value`() {
        assertEquals(96.dp, Dimensions.ThumbnailLG)
    }

    @Test
    fun `ThumbnailXL has correct value`() {
        assertEquals(128.dp, Dimensions.ThumbnailXL)
    }

    // endregion

    // region Divider and Border Tests

    @Test
    fun `DividerThickness has correct value`() {
        assertEquals(1.dp, Dimensions.DividerThickness)
    }

    @Test
    fun `BorderWidth has correct value`() {
        assertEquals(1.dp, Dimensions.BorderWidth)
    }

    @Test
    fun `BorderWidthThick has correct value`() {
        assertEquals(2.dp, Dimensions.BorderWidthThick)
    }

    // endregion

    // region Accessibility Tests

    @Test
    fun `MinTouchTarget has correct value for accessibility`() {
        assertEquals(48.dp, Dimensions.MinTouchTarget)
    }

    // endregion

    // region Screen Padding Tests

    @Test
    fun `ScreenPaddingHorizontal has correct value`() {
        assertEquals(16.dp, Dimensions.ScreenPaddingHorizontal)
    }

    @Test
    fun `ScreenPaddingTop has correct value`() {
        assertEquals(0.dp, Dimensions.ScreenPaddingTop)
    }

    @Test
    fun `ScreenPaddingBottom has correct value`() {
        assertEquals(16.dp, Dimensions.ScreenPaddingBottom)
    }

    // endregion

    // region Navigation Tests

    @Test
    fun `BottomNavHeight has correct value`() {
        assertEquals(80.dp, Dimensions.BottomNavHeight)
    }

    @Test
    fun `TopAppBarHeight has correct value`() {
        assertEquals(64.dp, Dimensions.TopAppBarHeight)
    }

    // endregion

    // region FAB Tests

    @Test
    fun `FabSize has correct value`() {
        assertEquals(56.dp, Dimensions.FabSize)
    }

    @Test
    fun `FabSizeMini has correct value`() {
        assertEquals(40.dp, Dimensions.FabSizeMini)
    }

    // endregion
}

/**
 * Tests for [Animation] duration constants.
 * Verifies that animation duration values are correct.
 */
class AnimationTest {

    @Test
    fun `Fast duration has correct value`() {
        assertEquals(100, Animation.Fast)
    }

    @Test
    fun `Normal duration has correct value`() {
        assertEquals(200, Animation.Normal)
    }

    @Test
    fun `Slow duration has correct value`() {
        assertEquals(400, Animation.Slow)
    }

    @Test
    fun `VerySlow duration has correct value`() {
        assertEquals(800, Animation.VerySlow)
    }
}

/**
 * Tests for [FontSize] constants.
 * Verifies that font size values are correct.
 */
class FontSizeTest {

    @Test
    fun `XS has correct value`() {
        assertEquals(10.sp, FontSize.XS)
    }

    @Test
    fun `SM has correct value`() {
        assertEquals(12.sp, FontSize.SM)
    }

    @Test
    fun `MD has correct value`() {
        assertEquals(14.sp, FontSize.MD)
    }

    @Test
    fun `LG has correct value`() {
        assertEquals(16.sp, FontSize.LG)
    }

    @Test
    fun `XL has correct value`() {
        assertEquals(18.sp, FontSize.XL)
    }

    @Test
    fun `XXL has correct value`() {
        assertEquals(20.sp, FontSize.XXL)
    }

    @Test
    fun `XXXL has correct value`() {
        assertEquals(24.sp, FontSize.XXXL)
    }

    @Test
    fun `Display has correct value`() {
        assertEquals(32.sp, FontSize.Display)
    }
}
