package com.bracketcove.graphsudoku.ui.activegame

import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash

/**
 * Holds the active game screen's state (board tiles, content state, timer, completion/record
 * flags) and exposes it to the Compose UI via callback setters (`sub*State`) rather than
 * [kotlinx.coroutines.flow.StateFlow]: each setter immediately invokes the callback with the
 * current value, and the corresponding `update*`/`show*` functions mutate state and re-invoke it.
 * [ActiveGameLogic] drives all mutations in response to [ActiveGameEvent]s and repository results.
 */
class ActiveGameViewModel {
    /** The current state of every tile on the board, keyed by node hash. */
    internal var boardState: HashMap<Int, SudokuTile> = HashMap()
    /** Callback invoked with [boardState] whenever it changes; also invoked immediately on assignment. */
    internal var subBoardState: ((HashMap<Int, SudokuTile>) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(boardState)
        }

    /** The current high-level screen state (loading/active/complete). */
    internal var contentState: ActiveGameScreenState = ActiveGameScreenState.LOADING
    /** Callback invoked with [contentState] whenever it changes; also invoked immediately on assignment. */
    internal var subContentState: ((ActiveGameScreenState) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(contentState)
        }

    /** The elapsed time, in seconds, for the current game. */
    internal var timerState: Long = 0L
    /** Callback invoked with [timerState] whenever it changes; also invoked immediately on assignment. */
    internal var subTimerState: ((Long) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(timerState)
        }

    /** Whether the current game has been completed. */
    internal var isCompleteState: Boolean = false
    /** Callback invoked with [isCompleteState] whenever it changes; also invoked immediately on assignment. */
    internal var subIsCompleteState: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(isCompleteState)
        }

    /** The difficulty of the current game. */
    internal var difficulty = Difficulty.MEDIUM
    /** The board's boundary (e.g. 9 for a standard 9x9 Sudoku). */
    internal var boundary = 9
    /** Whether completing the current game set a new best time. */
    internal var isNewRecordState: Boolean = false

    /**
     * Increments [timerState] by one second and notifies [subTimerState].
     */
    internal fun updateTimerState() {
        timerState++
        subTimerState?.invoke(timerState)
    }

    /**
     * Initializes [boardState], [contentState], [boundary], [difficulty] and [timerState] from a
     * freshly loaded [puzzle], and notifies all relevant subscribers.
     *
     * @param puzzle The puzzle to derive the board state from.
     * @param isComplete Whether the puzzle has already been completed.
     */
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

    /**
     * Updates the value and focus flag of the tile at ([x], [y]) and notifies [subBoardState].
     *
     * @param x The tile's x coordinate.
     * @param y The tile's y coordinate.
     * @param value The new value for the tile.
     * @param hasFocus Whether the tile should be marked as focused.
     */
    internal fun updateBoardState(x: Int, y: Int, value: Int, hasFocus: Boolean) {
        boardState[getHash(x, y)]?.let {
            it.value = value
            it.hasFocus = hasFocus
        }
        subBoardState?.invoke(boardState)
    }

    /**
     * Sets [contentState] to [ActiveGameScreenState.LOADING] and notifies [subContentState].
     */
    internal fun showLoadingState() {
        contentState = ActiveGameScreenState.LOADING
        subContentState?.invoke(contentState)
    }

    /**
     * Marks only the tile at ([x], [y]) as focused, clearing focus on every other tile, and
     * notifies [subBoardState].
     *
     * @param x The x coordinate of the tile to focus.
     * @param y The y coordinate of the tile to focus.
     */
    internal fun updateFocusState(x: Int, y: Int) {
        boardState.values.forEach {
            it.hasFocus = (it.x == x && it.y == y)
        }
        subBoardState?.invoke(boardState)
    }

    /**
     * Marks the current game as complete, transitioning [contentState] to
     * [ActiveGameScreenState.COMPLETE] and notifying [subContentState].
     */
    fun updateCompleteState() {
        isCompleteState = true
        contentState = ActiveGameScreenState.COMPLETE
        subContentState?.invoke(contentState)
    }
}

/**
 * Represents the UI state of a single Sudoku board tile.
 *
 * @param x The tile's x coordinate.
 * @param y The tile's y coordinate.
 * @param value The tile's current displayed value (0 typically meaning empty).
 * @param hasFocus Whether the tile currently has input focus.
 * @param readOnly Whether the tile is a pre-filled clue and cannot be edited by the user.
 */
class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    val readOnly: Boolean
)
