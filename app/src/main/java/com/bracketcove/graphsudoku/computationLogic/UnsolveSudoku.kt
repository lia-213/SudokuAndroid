package com.bracketcove.graphsudoku.computationLogic

import com.bracketcove.graphsudoku.domain.SudokuNode
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash
import java.util.LinkedList
import kotlin.random.Random

/**
 * Removes values from a solved [SudokuPuzzle] to create a playable puzzle based on difficulty.
 *
 * @return The [SudokuPuzzle] with some values removed.
 */
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

/**
 * Creates a deep copy of the [SudokuPuzzle].
 *
 * @return A new [SudokuPuzzle] instance with identical state.
 */
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

/**
 * Determines the solving strategy required for a puzzle.
 *
 * @param puzzle The [SudokuPuzzle] to analyze.
 * @return The [SolvingStrategy] required.
 */
internal fun determineDifficulty(puzzle: SudokuPuzzle): SolvingStrategy {
    if (isBasic(puzzle)) return SolvingStrategy.BASIC
    if (isAdvanced(puzzle)) return SolvingStrategy.ADVANCED
    return SolvingStrategy.UNSOLVABLE
}

/**
 * Checks if a puzzle can be solved using basic solving techniques (e.g., single possibility).
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if it's solvable with basic techniques, false otherwise.
 */
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

/**
 * Applies basic Sudoku solving logic to a clique of nodes.
 *
 * @param clique A list of conflicting nodes.
 * @param boundary The puzzle boundary.
 * @return True if a value was assigned, false otherwise.
 */
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

/**
 * Checks if a puzzle can be solved using advanced solving techniques.
 *
 * @param puzzle The [SudokuPuzzle] to check.
 * @return True if solvable with advanced techniques, false otherwise.
 */
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

/**
 * Applies advanced Sudoku solving logic.
 *
 * @param puzzle The [SudokuPuzzle].
 * @param superClique A larger group of conflicting nodes.
 * @param boundary The puzzle boundary.
 * @return True if an assignment was made, false otherwise.
 */
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

/**
 * Tests if assigning a pair of options to two nodes results in a valid puzzle state.
 *
 * @param options The two possible values.
 * @param firstNode The first node in the pair.
 * @param pairNode The second node in the pair.
 * @param puzzle The [SudokuPuzzle].
 * @return True if the assignments are valid, false otherwise.
 */
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

/**
 * Checks if two lists of options contain the same set of values.
 */
fun areSameOptions(firstOptions: List<Int>, secondOptions: List<Int>): Boolean {
    return firstOptions.containsAll(secondOptions) && secondOptions.containsAll(firstOptions)
}

/**
 * Retrieves a "super clique" of nodes for a given node, which includes its row, column, and subgrid nodes.
 *
 * @param first The source node.
 * @param puzzle The [SudokuPuzzle].
 * @return A [LinkedList] of all nodes conflicting with the source node.
 */
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
