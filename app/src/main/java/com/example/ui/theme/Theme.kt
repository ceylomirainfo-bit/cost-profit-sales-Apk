package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkForestGreenPrimary,
    onPrimary = ForestGreenOnContainer,
    primaryContainer = DarkForestGreenContainer,
    onPrimaryContainer = DarkForestGreenPrimary,
    secondary = DarkGoldSecondary,
    onSecondary = MutedGoldOnContainer,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = DarkGoldSecondary,
    tertiary = CinnamonContainer,
    background = DarkCharcoalBackground,
    onBackground = LightText,
    surface = DarkCreamSurface,
    onSurface = LightText,
    surfaceVariant = DarkCreamSurfaceVariant,
    onSurfaceVariant = LightText,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = MutedGoldSecondary,
    onSecondary = MutedGoldOnSecondary,
    secondaryContainer = MutedGoldContainer,
    onSecondaryContainer = MutedGoldOnContainer,
    tertiary = CinnamonTertiary,
    onTertiary = CinnamonOnTertiary,
    tertiaryContainer = CinnamonContainer,
    onTertiaryContainer = CinnamonOnTertiaryContainer,
    background = WarmIvoryBackground,
    onBackground = DarkCharcoalText,
    surface = CreamSurface,
    onSurface = DarkCharcoalText,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = DarkCharcoalTextSecondary,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Preserve custom luxury brand theme by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
