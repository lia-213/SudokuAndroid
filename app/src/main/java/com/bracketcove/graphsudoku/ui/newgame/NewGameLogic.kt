package com.bracketcove.graphsudoku.ui.newgame

import com.bracketcove.graphsudoku.common.BaseLogic
import com.bracketcove.graphsudoku.common.DispatcherProvider
import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.IGameRepository
import com.bracketcove.graphsudoku.domain.IStatisticsRepository
import com.bracketcove.graphsudoku.domain.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Business logic for the "new game" screen. Handles [NewGameEvent]s by reading and
 * persisting [Settings] and [com.bracketcove.graphsudoku.domain.UserStatistics] through
 * [gameRepo] and [statsRepo], mutating [viewModel]'s state, and notifying [container]
 * of navigation/error effects. Runs its coroutines on the UI dispatcher provided by
 * [dispatcher], tracked by a cancellable [jobTracker].
 */
class NewGameLogic(
    private val container: NewGameContainer?,
    private val viewModel: NewGameViewModel,
    private val gameRepo: IGameRepository,
    private val statsRepo: IStatisticsRepository,
    private val dispatcher: DispatcherProvider
) : BaseLogic<NewGameEvent>(),
    CoroutineScope {

    init {
        jobTracker = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = dispatcher.provideUIContext() + jobTracker

    /**
     * Dispatches an incoming [NewGameEvent] to the appropriate handler, or updates
     * [viewModel]'s settings state directly for simple field changes.
     *
     * @param event The UI event to handle.
     */
    override fun onEvent(event: NewGameEvent) {
        when (event) {
            is NewGameEvent.OnStart -> onStart()
            is NewGameEvent.OnDonePressed -> {
                viewModel.loadingState = true
                onDonePressed()
            }
            is NewGameEvent.OnSizeChanged -> viewModel.settingsState =
                viewModel.settingsState.copy(boundary = event.boundary)
            is NewGameEvent.OnDifficultyChanged -> viewModel.settingsState =
                viewModel.settingsState.copy(difficulty = event.diff)
        }
    }

    /**
     * Persists the current settings, then creates a new game if the save succeeds.
     * Notifies [container] and resets the loading state on failure.
     */
    private fun onDonePressed() {
        launch {
            gameRepo.updateSettings(
                viewModel.settingsState,
                {
                    createNewGame()
                },
                {
                    container?.showError()
                    viewModel.loadingState = false
                }
            )
        }
    }

    /**
     * Creates a new game using the current settings. On success, cancels [jobTracker]
     * and notifies [container] to navigate onward; on failure, surfaces an error.
     */
    private fun createNewGame() = launch {
        gameRepo.createNewGame(viewModel.settingsState,
            {
                jobTracker.cancel()
                container?.onDoneClick()
            },
            {
                android.util.Log.e("SUDOKU", "Failed to create new game", it)
                container?.showError()
                viewModel.loadingState = false
            }
        )
    }

    /**
     * Loads existing settings into [viewModel], falling back to sensible defaults
     * if none are found, then proceeds to load statistics.
     */
    private fun onStart() {
        launch {
            gameRepo.getSettings(
                {
                    viewModel.settingsState = it
                    getStatistics()
                },
                {
                    // If no settings found, use defaults instead of navigating back (avoids loop)
                    viewModel.settingsState = Settings(Difficulty.MEDIUM, 9)
                    getStatistics()
                }
            )
        }
    }

    /**
     * Loads user statistics into [viewModel] and clears the loading state once done,
     * regardless of success or failure.
     */
    private fun getStatistics() {
        launch {
            statsRepo.getStatistics(
                {
                    viewModel.statisticsState = it
                    viewModel.loadingState = false
                },
                {
                    container?.showError()
                    viewModel.loadingState = false
                }
            )
        }
    }
}
