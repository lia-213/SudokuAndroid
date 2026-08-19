package com.bracketcove.graphsudoku.ui.activegame

import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash

class ActiveGameViewModel {
    // "sub" prefixes will denote subject
    // virtual rep. of the sudoku board
    internal var subBoardState: ((HashMap<Int, SudokuTile>) -> Unit)? = null
    // 3 diff states: loading data, user has a currently active game they're solving, user has completed a particular game.will use this to animate between different states in the game
    internal var subContentState: ((ActiveGameScreenState) -> Unit)? = null
    // count-up timer, how long for the user to complete a particular game? how we will update the user interface after we update the timer state
    internal var subTimerState: ((Long) -> Unit)? = null

    internal fun updateTimerState() {
        // timerstate will be the long value representing time (passed)
        timerState++
        subTimerState?.invoke(timerState)
    }

    // default vals
    internal var subIsCompleteState: ((Boolean) -> Unit)? = null

    internal var timerState: Long = 0L

    internal var difficulty = Difficulty.MEDIUM
    internal var boundary = 9
    internal var boardState: HashMap<Int, SudokuTile> = HashMap()

    internal var isCompleteState: Boolean = false

    internal var isNewRecordState: Boolean = false

    fun initializeBoardState(
        puzzle: SudokuPuzzle,
        isComplete: Boolean
    ) {
        // taking the state of the data as it existed in storage,
        // giving it to the viewmodel
        // building the viewmodel's own internal representation of that state
        puzzle.graph.forEach {
            val node = it.value[0]
            boardState[it.key] = SudokuTile(
                node.x,
                node.y,
                node.color,
                hasFocus = false,
                node.readOnly
            )
        }

        val contentState: ActiveGameScreenState

        if (isComplete) {
            isCompleteState = true
            contentState = ActiveGameContentState.COMPLETE
        } else {
            contentState = ActiveGameContentState.ACTIVE
        }

        // binding that data to the viewmodel
        boundary = puzzle.boundary
        difficulty = puzzle.difficulty
        timerState = puzzle.elapsedTime

        // invoke function types to update hte view assuming it's listening

        subIsCompleteState?.invoke(isCompleteState)
        subContentState?.invoke(contentState)
        subBoardState?.invoke(boardState)
    }

    // qa few more functions that will be called by tthe presenter to do various things with the state of the viwmodel

    internal fun updateBoardState(
        x: Int,
        y: Int,
        value: Int,
        hasFocus: Boolean
    ) {
        boardState[getHash(x, y)]?.let {
            it.value = value
            it.hasFocus = hasFocus

        }

        subBoardState?.invoke(boardState)
    }
    internal fun showLoadingState() {
        subContentState?.invoke(ActiveGameScreenState.LOADING)
    }

    // when the user hits a particular tile, that sends a message into the presenter which will have a particular x and y coord.the presenter will call this particular function.
    // it iwll look for the tile the user clicked on based on that x and y value and set htat to hasFocus = true, then for every other tile, we will set it to false. a user can only select one file
    internal fun updateFocusState(x: Int, y: Int) {
        boardState.values.forEach{
            if (it.x == x && it.y == y) it.hasFocus = true
            else it.hasFocus = false
        }

        subBoardState?.invoke(boardState)
    }

    fun updateCompleteState() {
        isCompleteState = true
        subContentState?.invoke(ActiveGameScreenState.COMPLETE)
    }

}

class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    val readOnly: Boolean

)