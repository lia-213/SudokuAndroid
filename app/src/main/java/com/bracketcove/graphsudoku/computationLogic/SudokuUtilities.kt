package com.bracketcove.graphsudoku.computationLogic

import com.bracketcove.graphsudoku.domain.SudokuNode
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash
import java.util.*
import kotlin.math.sqrt

internal val Int.sqrt: Int
    get() = sqrt(this.toDouble()).toInt()


fun puzzleIsComplete(puzzle: SudokuPuzzle): Boolean {
    return puzzleIsValid(puzzle) && !hasEmptySquares(puzzle)
}

fun puzzleIsValid(puzzle: SudokuPuzzle): Boolean {
    return !rowsAreInvalid(puzzle) && !columnsAreInvalid(puzzle) && !subgridsAreInvalid(puzzle)
}

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

internal fun getNodesByColumn(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, x: Int): List<SudokuNode> {
    return graph.values.map { it.first() }.filter { it.x == x }
}

internal fun getNodesByRow(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, y: Int): List<SudokuNode> {
    return graph.values.map { it.first() }.filter { it.y == y }
}

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

internal fun getIntervalMax(boundary: Int, target: Int): Int {
    val interval = boundary.sqrt
    var i = interval
    while (i < target) {
        i += interval
    }
    return i
}

fun hasEmptySquares(puzzle: SudokuPuzzle): Boolean {
    return puzzle.graph.values.any { it.first().color == 0 }
}

//internal fun SudokuPuzzle.print() {
//    val sb = StringBuilder()
//    for (y in 1..boundary) {
//        for (x in 1..boundary) {
//            sb.append(this.graph[getHash(x, y)]?.firstOrNull()?.color ?: 0).append(" ")
//        }
//        sb.append("\n")
//    }
//    println(sb.toString())
//}

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
