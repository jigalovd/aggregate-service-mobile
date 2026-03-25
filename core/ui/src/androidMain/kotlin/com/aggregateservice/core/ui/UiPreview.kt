package com.aggregateservice.core.ui

import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview annotation for light theme previews.
 */
@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFFFFBFE,
)
annotation class LightPreview

/**
 * Preview annotation for dark theme previews.
 */
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
)
annotation class DarkPreview

/**
 * Preview annotation for both light and dark themes.
 */
@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFFFFBFE,
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
)
annotation class ThemePreviews

/**
 * Preview annotation for different font scales.
 */
@Preview(name = "Default", fontScale = 1.0f)
@Preview(name = "Large", fontScale = 1.5f)
@Preview(name = "Extra Large", fontScale = 2.0f)
annotation class FontScalePreviews

/**
 * Preview annotation for RTL layout.
 */
@Preview(name = "LTR", locale = "en")
@Preview(name = "RTL", locale = "he")
annotation class RtlPreviews
