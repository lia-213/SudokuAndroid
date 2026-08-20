package com.bracketcove.graphsudoku.domain

import java.io.Serializable

/**
 * Represents a single cell (node) in a Sudoku puzzle.
 *
 * @property x The horizontal coordinate (1-based).
 * @property y The vertical coordinate (1-based).
 * @property color The value of the node (0 if empty).
 * @property readOnly True if the node is part of the initial puzzle clues.
 */
data class SudokuNode(
    val x: Int,
    val y: Int,
    // value (int) from 0 to the boundary of the puzzle - color wrong name really
    var color: Int = 0,
    // readOnly true -> a hint given at the start
    var readOnly: Boolean = true,
): Serializable {
    override fun hashCode(): Int {
        return getHash(x, y)
    }
}

/**
 * Generates a unique hash for a node based on its coordinates.
 *
 * @param x The x coordinate.
 * @param y The y coordinate.
 * @return A unique integer hash.
 */
internal fun getHash(x: Int, y: Int): Int {
    // multiply x by 100 to mitigate for edge cases in larger boards than 9x9 (e.g. 16x16)
    // making the hashcode unique for every cell in the puzzle
    val newX = x*100
    return "$newX$y".toInt()
}
