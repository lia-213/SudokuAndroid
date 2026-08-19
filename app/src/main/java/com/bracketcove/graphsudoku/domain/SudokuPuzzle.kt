package com.bracketcove.graphsudoku.domain

import java.io.Serializable
import java.util.LinkedList

// data models are virtual representations of real-world objects, e.g. sudoku puzzle
data class SudokuPuzzle(
    val boundary: Int,
    val difficulty: Difficulty,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    var elapsedTime: Long = 0L
): Serializable {
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
    fun print(): String = "SudokuPuzzle(boundary=$boundary, difficulty=$difficulty)"
}
