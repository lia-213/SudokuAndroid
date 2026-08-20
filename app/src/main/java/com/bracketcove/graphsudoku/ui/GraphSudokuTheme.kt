package com.bracketcove.graphsudoku.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Material 3 [androidx.compose.material3.ColorScheme] applied when the app is in light mode.
 */
private val LightColorScheme = lightColorScheme(
    primary = primaryGreen,
    secondary = textColorLight,
    surface = lightGrey,
    onPrimary = accentAmber,
    onSurface = accentAmber,
    outline = gridLineColorLight
)

/**
 * Material 3 [androidx.compose.material3.ColorScheme] applied when the app is in dark mode.
 */
private val DarkColorScheme = darkColorScheme(
    primary = primaryCharcoal,
    secondary = textColorDark,
    surface = lightGreyAlpha,
    onPrimary = accentAmber,
    onSurface = accentAmber,
    outline = gridLineColorLight
)

/**
 * App-wide [MaterialTheme] wrapper that applies the correct color scheme, typography, and shapes
 * based on whether the system is in dark mode.
 *
 * @param darkTheme Whether to use [DarkColorScheme] instead of [LightColorScheme].
 * @param content The themed composable content.
 */
@Composable
fun GraphSudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
