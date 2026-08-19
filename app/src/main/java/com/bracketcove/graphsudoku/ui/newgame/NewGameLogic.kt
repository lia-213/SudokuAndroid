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
