package com.bracketcove.graphsudoku

import com.bracketcove.graphsudoku.computationLogic.*
import com.bracketcove.graphsudoku.domain.*
import com.bracketcove.graphsudoku.common.*
import org.junit.Test
import java.util.*

/**
 * Comprehensive unit tests for the Sudoku generation and solving algorithms.
 * Verifies graph building, clue removal (unsolving), and basic utility functions.
 */
class GraphSudokuAlgorithmTests {

    @Test
    fun unsolverTest(){
        buildSudoku(9, Difficulty.MEDIUM)
    }

    @Test
    fun getPossibleTestValues() {
        val puzzle = buildSudoku(4, Difficulty.EASY)
        puzzle.graph.values.forEach { it.first().color = 0 }

        puzzle.graph[getHash(2, 1)]!!.first().color = 2
        puzzle.graph[getHash(2, 2)]!!.first().color = 1
        puzzle.graph[getHash(3, 1)]!!.first().color = 1
        puzzle.graph[getHash(4, 2)]!!.first().color = 3
        puzzle.graph[getHash(1, 4)]!!.first().color = 2
        puzzle.graph[getHash(3, 3)]!!.first().color = 4

        println(puzzle.print())
    }

    @Test
    fun testSuperCliqueCountOccurences() {
        val puzzle = buildSudoku(4, Difficulty.EASY)
        val firstNode = puzzle.graph.values.first().first()
        val superClique = getSuperClique(firstNode, puzzle)
        val boundary = puzzle.boundary
        val key = firstNode
        val iMaxX = getIntervalMax(boundary, key.x)
        val iMaxY = getIntervalMax(boundary, key.y)

        val count = superClique.filter { node ->
            when {
                (node.x == key.x && node.y != key.y) -> true
                (node.x != key.x && node.y == key.y) -> true
                (iMaxX == getIntervalMax(boundary, node.x) && iMaxY == getIntervalMax(boundary, node.y)) -> true
                else -> false
            }
        }.count()

        assert(count >= 7)
    }

    @Test
    fun difficultyTests() {
        // 4x4 tests (16 tiles total)
        val easy4 = buildSudoku(4, Difficulty.EASY).graph.values.count { it.first().color != 0 }
        assert(easy4 == (16 * Difficulty.EASY.modifier).toInt())

        val med4 = buildSudoku(4, Difficulty.MEDIUM).graph.values.count { it.first().color != 0 }
        assert(med4 == (16 * Difficulty.MEDIUM.modifier).toInt())

        val hard4 = buildSudoku(4, Difficulty.HARD).graph.values.count { it.first().color != 0 }
        assert(hard4 == (16 * Difficulty.HARD.modifier).toInt())

        // 9x9 tests (81 tiles total)
        val easy9 = buildSudoku(9, Difficulty.EASY).graph.values.count { it.first().color != 0 }
        assert(easy9 == (81 * Difficulty.EASY.modifier).toInt())

        val med9 = buildSudoku(9, Difficulty.MEDIUM).graph.values.count { it.first().color != 0 }
        assert(med9 == (81 * Difficulty.MEDIUM.modifier).toInt())

        val hard9 = buildSudoku(9, Difficulty.HARD).graph.values.count { it.first().color != 0 }
        assert(hard9 == (81 * Difficulty.HARD.modifier).toInt())
    }

    @Test
    fun verifySolverAlgorithm() {
        val fourGraph = buildSudoku(4, Difficulty.MEDIUM)
        fourGraph.graph.values.forEach {
            assert(it.first().color != 0 || !it.first().readOnly)
        }

        val nineGraph = buildSudoku(9, Difficulty.MEDIUM)
        nineGraph.graph.values.forEach {
            assert(it.first().color != 0 || !it.first().readOnly)
        }
    }

    @Test
    fun verifyGraphSize() {
        assert(buildSudoku(4, Difficulty.MEDIUM).graph.size == 16)
        assert(buildSudoku(9, Difficulty.MEDIUM).graph.size == 81)
        assert(buildSudoku(16, Difficulty.MEDIUM).graph.size == 256)
    }

    @Test
    fun testHash() {
        val first = SudokuNode(1, 4)
        assert(first.hashCode() == 1004)
    }

    @Test
    fun getIntervalMaxTest() {
        assert(getIntervalMax(4, 1) == 2)
        assert(getIntervalMax(9, 5) == 6)
        assert(getIntervalMax(16, 2) == 4)
    }

    @Test
    fun mergeTest() {
        val firstList = LinkedList<SudokuNode>()
        firstList.add(SudokuNode(1, 1, 0))
        val secondList = listOf(
            SudokuNode(1, 1, 0),
            SudokuNode(1, 2, 0),
            SudokuNode(1, 3, 0),
            SudokuNode(1, 4, 0)
        )

        firstList.mergeWithoutRepeats(secondList)
        assert(firstList.size == 4)
    }

    @Test
    fun verifyEdgesBuilt() {
        val fourGraph = buildSudoku(4, Difficulty.MEDIUM)
        val nineGraph = buildSudoku(9, Difficulty.MEDIUM)
        val sixteenGraph = buildSudoku(16, Difficulty.MEDIUM)

        fourGraph.graph.forEach {
            assert(it.value.size == 8)
        }

        nineGraph.graph.forEach {
            assert(it.value.size == 21)
        }

        sixteenGraph.graph.forEach {
            assert(it.value.size == 40)
        }
    }
}
