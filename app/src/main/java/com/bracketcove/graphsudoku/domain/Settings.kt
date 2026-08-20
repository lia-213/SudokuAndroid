package com.bracketcove.graphsudoku.domain

/**
 * Data model representing the user's game settings.
 *
 * @property difficulty The chosen difficulty level.
 * @property boundary The size of the Sudoku grid (e.g., 4x4 or 9x9).
 */
data class Settings(
    val difficulty: Difficulty,
    val boundary: Int
)
