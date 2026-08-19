package com.bracketcove.graphsudoku.ui.activegame

import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash

class ActiveGameViewModel {
    internal var boardState: HashMap<Int, SudokuTile> = HashMap()
    internal var subBoardState: ((HashMap<Int, SudokuTile>) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(boardState)
        }

    internal var contentState: ActiveGameScreenState = ActiveGameScreenState.LOADING
    internal var subContentState: ((ActiveGameScreenState) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(contentState)
        }

    internal var timerState: Long = 0L
    internal var subTimerState: ((Long) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(timerState)
        }

    internal var isCompleteState: Boolean = false
    internal var subIsCompleteState: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(isCompleteState)
        }

    internal var difficulty = Difficulty.MEDIUM
    internal var boundary = 9
    internal var isNewRecordState: Boolean = false

    internal fun updateTimerState() {
        timerState++
        subTimerState?.invoke(timerState)
    }

    fun initializeBoardState(puzzle: SudokuPuzzle, isComplete: Boolean) {
        val newBoardState = HashMap<Int, SudokuTile>()
        puzzle.graph.forEach {
            val node = it.value[0]
            newBoardState[it.key] = SudokuTile(
                node.x,
                node.y,
                node.color,
                hasFocus = false,
                node.readOnly
            )
        }
        boardState = newBoardState

        contentState = if (isComplete) {
            isCompleteState = true
            ActiveGameScreenState.COMPLETE
        } else {
            ActiveGameScreenState.ACTIVE
        }

        boundary = puzzle.boundary
        difficulty = puzzle.difficulty
        timerState = puzzle.elapsedTime

        subIsCompleteState?.invoke(isCompleteState)
        subContentState?.invoke(contentState)
        subBoardState?.invoke(boardState)
    }

    internal fun updateBoardState(x: Int, y: Int, value: Int, hasFocus: Boolean) {
        boardState[getHash(x, y)]?.let {
            it.value = value
            it.hasFocus = hasFocus
        }
        subBoardState?.invoke(boardState)
    }

    internal fun showLoadingState() {
        contentState = ActiveGameScreenState.LOADING
        subContentState?.invoke(contentState)
    }

    internal fun updateFocusState(x: Int, y: Int) {
        boardState.values.forEach {
            it.hasFocus = (it.x == x && it.y == y)
        }
        subBoardState?.invoke(boardState)
    }

    fun updateCompleteState() {
        isCompleteState = true
        contentState = ActiveGameScreenState.COMPLETE
        subContentState?.invoke(contentState)
    }
}

class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    val readOnly: Boolean
)
