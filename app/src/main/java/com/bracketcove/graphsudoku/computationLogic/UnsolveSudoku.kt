package com.bracketcove.graphsudoku.computationLogic

import com.bracketcove.graphsudoku.domain.SudokuNode
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash
import java.util.LinkedList
import kotlin.random.Random

internal fun SudokuPuzzle.unsolve(): SudokuPuzzle {
    // 1. Calculate how many clues to remove based on difficulty
    val totalTiles = boundary * boundary
    val targetClues = (totalTiles * difficulty.modifier).toInt()
    val tilesToRemove = totalTiles - targetClues
    
    var removedCount = 0
    val random = Random.Default

    // 2. Simple clue removal loop (no recursion!)
    // We try to remove tiles until we hit our target or 1000 attempts
    var attempts = 0
    while (removedCount < tilesToRemove && attempts < 1000) {
        attempts++
        val x = random.nextInt(1, boundary + 1)
        val y = random.nextInt(1, boundary + 1)
        
        val node = this.graph[getHash(x, y)]?.firstOrNull()
        
        if (node != null && node.color != 0) {
            node.color = 0
            node.readOnly = false
            removedCount++
        }
    }
    
    return this
}

// Keeping helper functions for other logic but removed all recursive difficulty checks for now
internal fun SudokuPuzzle.deepCopy(): SudokuPuzzle {
    val newMap = LinkedHashMap<Int, LinkedList<SudokuNode>>()
    this.graph.forEach { (key, list) ->
        val newList = LinkedList<SudokuNode>()
        list.forEach { node ->
            newList.add(SudokuNode(node.x, node.y, node.color, node.readOnly))
        }
        newMap[key] = newList
    }
    return SudokuPuzzle(this.boundary, this.difficulty, newMap, this.elapsedTime)
}

internal fun determineDifficulty(puzzle: SudokuPuzzle): SolvingStrategy {
    if (isBasic(puzzle)) return SolvingStrategy.BASIC
    if (isAdvanced(puzzle)) return SolvingStrategy.ADVANCED
    return SolvingStrategy.UNSOLVABLE
}

internal fun isBasic(puzzle: SudokuPuzzle): Boolean {
    var solveable = true
    while (solveable) {
        solveable = false
        puzzle.graph.values.forEach {
            if (basicSolver(it, puzzle.boundary)) solveable = true
        }
        if (puzzleIsComplete(puzzle)) return true
    }
    return false
}

internal fun basicSolver(clique: LinkedList<SudokuNode>, boundary: Int): Boolean {
    if (clique.firstOrNull()?.color == 0) {
        val options = getPossibleValues(clique, boundary)
        if (options.size == 1) {
            clique.first().color = options.first()
            return true
        }
    }
    return false
}

internal fun isAdvanced(puzzle: SudokuPuzzle): Boolean {
    var solveable = true
    while (solveable) {
        solveable = false
        puzzle.graph.values.filter { it.firstOrNull()?.color == 0 }.forEach {
            if (basicSolver(it, puzzle.boundary)) solveable = true
            else {
                val superClique = getSuperClique(it.first(), puzzle)
                if (advancedSolver(puzzle, superClique, puzzle.boundary)) solveable = true
            }
        }
        if (puzzleIsComplete(puzzle)) return true
    }
    return false
}

fun advancedSolver(puzzle: SudokuPuzzle, superClique: LinkedList<SudokuNode>, boundary: Int): Boolean {
    val firstNode = superClique.firstOrNull() ?: return false
    val firstOptions = getPossibleValues(firstNode, superClique, boundary)
    if (firstOptions.size != 2) return false

    val pairs = mutableListOf<SudokuNode>()
    superClique.forEach { node ->
        if (node.color == 0 && node != firstNode) {
            val secondOptions = getPossibleValues(node, superClique, boundary)
            if (secondOptions.size == 2 && areSameOptions(firstOptions, secondOptions)) {
                pairs.add(node)
            }
        }
    }

    if (pairs.isEmpty()) return false

    pairs.forEach { pairNode ->
        if (testPair(firstOptions, firstNode, pairNode, puzzle)) return true
    }
    return false
}

fun testPair(options: List<Int>, firstNode: SudokuNode, pairNode: SudokuNode, puzzle: SudokuPuzzle): Boolean {
    firstNode.color = options[0]
    pairNode.color = options[1]
    if (puzzleIsValid(puzzle)) return true

    firstNode.color = options[1]
    pairNode.color = options[0]
    if (puzzleIsValid(puzzle)) return true

    firstNode.color = 0
    pairNode.color = 0
    return false
}

fun areSameOptions(firstOptions: List<Int>, secondOptions: List<Int>): Boolean {
    return firstOptions.containsAll(secondOptions) && secondOptions.containsAll(firstOptions)
}

internal fun getSuperClique(first: SudokuNode, puzzle: SudokuPuzzle): LinkedList<SudokuNode> {
    val superClique = LinkedList<SudokuNode>()
    superClique.add(first)
    val iMaxX = getIntervalMax(puzzle.boundary, first.x)
    val iMaxY = getIntervalMax(puzzle.boundary, first.y)

    ((iMaxX - puzzle.boundary.sqrt) + 1..iMaxX).forEach { xIndex ->
        (1..puzzle.boundary).forEach { yIndex ->
            val node = puzzle.graph[getHash(xIndex, yIndex)]?.firstOrNull()
            if (node != null && !superClique.contains(node)) superClique.add(node)
        }
    }

    ((iMaxY - puzzle.boundary.sqrt) + 1..iMaxY).forEach { yIndex ->
        (1..puzzle.boundary).forEach { xIndex ->
            val node = puzzle.graph[getHash(xIndex, yIndex)]?.firstOrNull()
            if (node != null && !superClique.contains(node)) superClique.add(node)
        }
    }
    return superClique
}

enum class SolvingStrategy {
    BASIC,
    ADVANCED,
    UNSOLVABLE
}
