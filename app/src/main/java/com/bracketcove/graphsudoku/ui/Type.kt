package com.bracketcove.graphsudoku.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Set of Material 3 typography styles to start with.
 */
val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Custom text styles used for specific screens and widgets, such as statistics labels,
 * number-pad buttons, screen titles/subtitles, and the victory banner.
 */
val statsLabel = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 24.sp,
    textAlign = TextAlign.Center
)

val inputButton = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Black,
    fontSize = 28.sp,
    textAlign = TextAlign.Center
)

val newGameSubtitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 32.sp,
    textAlign = TextAlign.Start
)

val activeGameSubtitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 26.sp,
    textAlign = TextAlign.Center,
)

val mainTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 48.sp,
    textAlign = TextAlign.Center
)

/**
 * Text style for dropdown menu items, allowing the text color to be customized per-item.
 *
 * @param color The color to apply to the dropdown text.
 * @return A [TextStyle] configured for dropdown menu text.
 */
fun dropdownText(color: Color) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    textAlign = TextAlign.Center,
    color = color
)

/**
 * Text style for a read-only (pre-filled) Sudoku square, scaling font size to the tile's size.
 *
 * @param tileOffset The pixel size of the square tile, used to derive a proportional font size.
 * @return A [TextStyle] configured for a read-only Sudoku square.
 */
fun readOnlySudokuSquare(tileOffset: Float) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = (tileOffset * .75).sp,
    textAlign = TextAlign.Center,
    color = Color.Black
)

/**
 * Text style for a mutable (user-editable) Sudoku square, scaling font size to the tile's size.
 *
 * @param tileOffset The pixel size of the square tile, used to derive a proportional font size.
 * @return A [TextStyle] configured for a mutable Sudoku square.
 */
fun mutableSudokuSquare(tileOffset: Float) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = (tileOffset * .75).sp,
    textAlign = TextAlign.Center,
)
