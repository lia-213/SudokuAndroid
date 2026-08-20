package com.bracketcove.graphsudoku.computationLogic

import com.bracketcove.graphsudoku.domain.SudokuNode
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash
import java.util.*
import kotlin.math.sqrt

/**
 * Extension property for [Int] to calculate its integer square root.
 */
internal val Int.sqrt: Int
    get() = sqrt(this.toDouble()).toInt()

/**
 * Checks if a Sudoku puzzle is complete and valid.
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if the puzzle is valid and has no empty squares, false otherwise.
 */
fun puzzleIsComplete(puzzle: SudokuPuzzle): Boolean {
    return puzzleIsValid(puzzle) && !hasEmptySquares(puzzle)
}

/**
 * Checks if a Sudoku puzzle is valid according to Sudoku rules (rows, columns, subgrids).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if the puzzle is valid, false otherwise.
 */
fun puzzleIsValid(puzzle: SudokuPuzzle): Boolean {
    return !rowsAreInvalid(puzzle) && !columnsAreInvalid(puzzle) && !subgridsAreInvalid(puzzle)
}

/**
 * Checks if any row in the puzzle is invalid (contains duplicate values).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if any row is invalid, false otherwise.
 */
fun rowsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    for (y in 1..puzzle.boundary) {
        val values = mutableListOf<Int>()
        for (x in 1..puzzle.boundary) {
            val color = puzzle.graph[getHash(x, y)]?.firstOrNull()?.color ?: 0
            if (color != 0) {
                if (values.contains(color)) return true
                values.add(color)
            }
        }
    }
    return false
}

/**
 * Checks if any column in the puzzle is invalid (contains duplicate values).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if any column is invalid, false otherwise.
 */
fun columnsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    for (x in 1..puzzle.boundary) {
        val values = mutableListOf<Int>()
        for (y in 1..puzzle.boundary) {
            val color = puzzle.graph[getHash(x, y)]?.firstOrNull()?.color ?: 0
            if (color != 0) {
                if (values.contains(color)) return true
                values.add(color)
            }
        }
    }
    return false
}

/**
 * Checks if any subgrid in the puzzle is invalid (contains duplicate values).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if any subgrid is invalid, false otherwise.
 */
fun subgridsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    val interval = puzzle.boundary.sqrt
    for (i in 0 until interval) {
        for (j in 0 until interval) {
            val values = mutableListOf<Int>()
            for (x in (i * interval + 1)..(i * interval + interval)) {
                for (y in (j * interval + 1)..(j * interval + interval)) {
                    val color = puzzle.graph[getHash(x, y)]?.firstOrNull()?.color ?: 0
                    if (color != 0) {
                        if (values.contains(color)) return true
                        values.add(color)
                    }
                }
            }
        }
    }
    return false
}

/**
 * Retrieves all nodes belonging to a specific column.
 *
 * @param graph The puzzle's graph representation.
 * @param x The column index.
 * @return A list of [SudokuNode] in the specified column.
 */
internal fun getNodesByColumn(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, x: Int): List<SudokuNode> {
    return graph.values.map { it.first() }.filter { it.x == x }
}

/**
 * Retrieves all nodes belonging to a specific row.
 *
 * @param graph The puzzle's graph representation.
 * @param y The row index.
 * @return A list of [SudokuNode] in the specified row.
 */
internal fun getNodesByRow(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, y: Int): List<SudokuNode> {
    return graph.values.map { it.first() }.filter { it.y == y }
}

/**
 * Retrieves all nodes belonging to a specific subgrid based on a node's coordinates.
 *
 * @param graph The puzzle's graph representation.
 * @param x The node's x coordinate.
 * @param y The node's y coordinate.
 * @param boundary The boundary of the puzzle.
 * @return A list of [SudokuNode] in the specified subgrid.
 */
internal fun getNodesBySubgrid(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, x: Int, y: Int, boundary: Int): List<SudokuNode> {
    val edgeList = mutableListOf<SudokuNode>()
    val iMaxX = getIntervalMax(boundary, x)
    val iMaxY = getIntervalMax(boundary, y)
    val interval = boundary.sqrt

    for (xIdx in (iMaxX - interval + 1)..iMaxX) {
        for (yIdx in (iMaxY - interval + 1)..iMaxY) {
            graph[getHash(xIdx, yIdx)]?.firstOrNull()?.let { edgeList.add(it) }
        }
    }
    return edgeList
}

/**
 * Calculates the maximum index of the subgrid interval for a given coordinate.
 *
 * @param boundary The boundary of the puzzle.
 * @param target The coordinate index.
 * @return The maximum index of the interval.
 */
internal fun getIntervalMax(boundary: Int, target: Int): Int {
    val interval = boundary.sqrt
    var i = interval
    while (i < target) {
        i += interval
    }
    return i
}

/**
 * Checks if the puzzle has any empty squares (nodes with color 0).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if there are empty squares, false otherwise.
 */
fun hasEmptySquares(puzzle: SudokuPuzzle): Boolean {
    return puzzle.graph.values.any { it.first().color == 0 }
}

/**
 * Calculates the possible values (colors) for a node based on its conflicts (adjacency list).
 *
 * @param adjList The adjacency list for a node (including the node itself).
 * @param boundary The boundary of the puzzle.
 * @return A list of valid values for the node.
 */
fun getPossibleValues(adjList: LinkedList<SudokuNode>, boundary: Int): List<Int> {
    val options = mutableListOf<Int>()
    val node = adjList.first()
    val originalColor = node.color
    for (i in 1..boundary) {
        node.color = i
        // Since adjList is a list of conflicting nodes, we only need to check if any other node has this color
        val conflict = adjList.drop(1).any { it.color == i }
        if (!conflict) options.add(i)
    }
    node.color = originalColor
    return options
}

/**
 * Calculates the possible values (colors) for a specific node given an adjacency list.
 *
 * @param key The node to find values for.
 * @param adjList The list of conflicting nodes.
 * @param boundary The boundary of the puzzle.
 * @return A list of valid values for the node.
 */
fun getPossibleValues(key: SudokuNode, adjList: LinkedList<SudokuNode>, boundary: Int): List<Int> {
    val options = mutableListOf<Int>()
    val originalColor = key.color
    for (i in 1..boundary) {
        key.color = i
        val conflict = adjList.filter { it != key }.any { it.color == i }
        if (!conflict) options.add(i)
    }
    key.color = originalColor
    return options
}
