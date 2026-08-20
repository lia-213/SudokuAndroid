package com.bracketcove.graphsudoku.ui.activegame

import com.bracketcove.graphsudoku.common.BaseLogic
import com.bracketcove.graphsudoku.common.DispatcherProvider
import com.bracketcove.graphsudoku.domain.IGameRepository
import com.bracketcove.graphsudoku.domain.IStatisticsRepository
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Presentation logic for the active game feature. Coordinates between the [ActiveGameContainer]
 * (Android-specific navigation/error display), the [ActiveGameViewModel] (screen state exposed to
 * the Compose UI via callback setters), and the backend ([IGameRepository], [IStatisticsRepository]).
 * Handles [ActiveGameEvent]s dispatched from the UI, including running and persisting the game timer.
 */
class ActiveGameLogic(
    private val container: ActiveGameContainer?,
    private val viewModel: ActiveGameViewModel,
    private val gameRepo: IGameRepository,
    private val statsRepo: IStatisticsRepository,
    private val dispatcher: DispatcherProvider
) : BaseLogic<ActiveGameEvent>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = dispatcher.provideUIContext() + jobTracker

    init {
        jobTracker = Job()
    }

    // android-specific timer classes introduced diff problems in implementation
    // inline: copy-paste
    // crossinline function type:
    // ----- in the lambda function, we can't wrtie a recurrent statement. taking a preventative step  for unexpected behaviour
    /**
     * Launches a coroutine that repeatedly invokes [action] once per second until cancelled.
     * Used to drive the game timer without relying on Android-specific timer classes.
     *
     * @param action The block to run on every tick.
     * @return The [Job] representing the running timer coroutine.
     */
    inline fun startCoroutineTimer(
        crossinline action: () -> Unit
        // launch starts a coroutine - it says "run whatever is inside this block in the background."
    ) = launch {
        while (true) {
            action()
            // delaying the coroutine doesn't block the thread we are on!
            delay(1000)
        }
    }

    // how to sstop the above coroutine
    private var timerTracker: Job? = null

    /**
     * Converts a raw timer value into the offset actually persisted/displayed, correcting for the
     * extra second accrued between the last tick and the game being stopped.
     */
    private val Long.timeOffset: Long
        get() {
            return if (this <= 0) 0
            else this - 1
        }

    /**
     * Routes an incoming [ActiveGameEvent] to its corresponding handler.
     *
     * @param event The UI event to process.
     */
    override fun onEvent(event: ActiveGameEvent) {
        when (event) {
            is ActiveGameEvent.OnInput -> onInput(
                event.input,
                viewModel.timerState
            )

            ActiveGameEvent.OnNewGameClicked -> onNewGameClicked()
            ActiveGameEvent.OnStart -> onStart()
            ActiveGameEvent.OnStop -> onStop()
            is ActiveGameEvent.OnTileFocused -> onTileFocused(event.x, event.y)
        }
    }

    /**
     * Marks the tile at ([x], [y]) as focused so subsequent number input applies to it.
     *
     * @param x The tile's x coordinate.
     * @param y The tile's y coordinate.
     */
    private fun onTileFocused(x: Int, y: Int) {
        viewModel.updateFocusState(x, y)
    }

    // save user's current progress and shut the app down
    /**
     * Persists the current game progress (if the puzzle isn't already complete) and then cancels
     * all running coroutines, including the timer.
     */
    private fun onStop() {
        if (!viewModel.isCompleteState) {
            launch {
                gameRepo.saveGame(
                    viewModel.timerState.timeOffset,
                    { cancelStuff() },
                    {
                        cancelStuff()
                        container?.showError()
                    }
                )
            }
        } else {
            cancelStuff()
        }
    }


    /**
     * Loads the current game and initializes the board state. If a game is in progress, starts the
     * timer; if it's complete, leaves the timer stopped. If no current game exists, asks the
     * [container] to navigate to the new game screen.
     */
    private fun onStart() = launch {
        gameRepo.getCurrentGame(
            { puzzle, isComplete ->
                viewModel.initializeBoardState(
                    puzzle,
                    isComplete
                )
                if (!isComplete) timerTracker = startCoroutineTimer {
                    viewModel.updateTimerState()
                }
            },
            // ask the storage for a current game and there is no game to retrieve? not started before - user running for the first time
            {
                android.util.Log.d("SUDOKU", "No current game found, redirecting to new game")
                container?.onNewGameClick()
            }
        )
    }

    /**
     * Handles the user requesting a new game: shows a loading state, saves progress on the current
     * game if it isn't complete yet, then navigates to the new game screen.
     */
    private fun onNewGameClicked() = launch {
        viewModel.showLoadingState()

        // if the user hasn't completed the current game, store the progress they have made in the current game when they hit onNewGameclick in case they hit it accidentally or want to go back later to finish
        if (!viewModel.isCompleteState) {
            gameRepo.getCurrentGame(
                { puzzle, _ ->
                    updateWithTime(puzzle)
                },
                {
                    container?.showError()
                }
            )
        } else {
            navigateToNewGame()
        }
    }

    /**
     * Persists [puzzle] with its elapsed time updated to the current timer value, then navigates
     * to the new game screen.
     *
     * @param puzzle The current puzzle to update before navigating away.
     */
    private fun updateWithTime(puzzle: SudokuPuzzle) = launch {
        gameRepo.updateGame(
            puzzle.copy(elapsedTime = viewModel.timerState.timeOffset),
            { navigateToNewGame() },
            {
                container?.showError()
                navigateToNewGame()
            }
        )
    }

    /**
     * Cancels all running coroutines and asks the [container] to navigate to the new game screen.
     */
    private fun navigateToNewGame() {
        cancelStuff()
        container?.onNewGameClick()
    }

    // cancels every coroutine
    /**
     * Cancels the timer coroutine (if running) and every coroutine tracked by [jobTracker].
     */
    private fun cancelStuff() {
        if (timerTracker?.isCancelled == false) timerTracker?.cancel()
        jobTracker.cancel()
    }

    // "when the user inputs a number and a timer value, start a background coroutine to handle it."
    /**
     * Applies [input] to the currently focused tile, updating the board state and, if the puzzle
     * is now complete, stopping the timer and checking for a new record.
     *
     * @param input The value entered by the user.
     * @param elapsedTime The current timer value, persisted alongside the node update.
     */
    private fun onInput(input: Int, elapsedTime: Long) = launch {
        var focusedTile: SudokuTile? = null
        viewModel.boardState.values.forEach {
            if (it.hasFocus) focusedTile = it
        }

        if (focusedTile != null) {
            gameRepo.updateNode(
                focusedTile!!.x,
                focusedTile!!.y,
                input,
                elapsedTime,
                //success
                { isComplete ->
                    focusedTile?.let {
                        viewModel.updateBoardState(
                            it.x,
                            it.y,
                            input,
                            false
                        )

                        if (isComplete) {
                            timerTracker?.cancel()
                            checkIfNewRecord()
                        }
                    }
                },
                { container?.showError() }
            )
        }
    }


    /**
     * Updates the difficulty/boundary's statistic with the completed game's time, updates the
     * "new record" flag, and marks the game as complete in the [viewModel].
     */
    private fun checkIfNewRecord() = launch {
        statsRepo.updateStatistic(
            viewModel.timerState,
            viewModel.difficulty,
            viewModel.boundary,
            //success
            { isRecord ->
                viewModel.isNewRecordState = isRecord
                viewModel.updateCompleteState()
            },
            //error
            {
                container?.showError()
                viewModel.updateCompleteState()
            }
        )
    }
}
