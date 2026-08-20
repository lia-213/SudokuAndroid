package com.bracketcove.graphsudoku.domain

import java.io.Serializable
import java.util.LinkedList

/**
 * Represents a complete Sudoku puzzle state.
 *
 * @property boundary The grid size boundary.
 * @property difficulty The puzzle difficulty.
 * @property graph The adjacency list representation of the puzzle grid.
 * @property elapsedTime The time spent solving the puzzle in milliseconds.
 */
data class SudokuPuzzle(
    val boundary: Int,
    val difficulty: Difficulty,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    var elapsedTime: Long = 0L
): Serializable {
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
    fun print(): String = "SudokuPuzzle(boundary=$boundary, difficulty=$difficulty)"
}
