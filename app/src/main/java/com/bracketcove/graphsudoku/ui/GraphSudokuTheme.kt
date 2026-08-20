package com.bracketcove.graphsudoku.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = primaryGreen,
    secondary = textColorLight,
    surface = lightGrey,
    onPrimary = accentAmber,
    onSurface = accentAmber,
    outline = gridLineColorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryCharcoal,
    secondary = textColorDark,
    surface = lightGreyAlpha,
    onPrimary = accentAmber,
    onSurface = accentAmber,
    outline = gridLineColorLight
)

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
