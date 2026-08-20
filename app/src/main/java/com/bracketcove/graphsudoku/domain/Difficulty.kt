package com.bracketcove.graphsudoku.domain

/**
 * Represents the difficulty levels for a Sudoku puzzle.
 *
 * @property modifier A value used to determine how many clues remain in the puzzle.
 */
enum class Difficulty(val modifier: Double) {
    EASY(0.5),
    MEDIUM(0.38),
    HARD(0.26)
}