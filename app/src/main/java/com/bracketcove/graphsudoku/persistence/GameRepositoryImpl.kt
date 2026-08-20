package com.bracketcove.graphsudoku.persistence

import com.bracketcove.graphsudoku.domain.GameStorageResult
import com.bracketcove.graphsudoku.domain.IGameDataStorage
import com.bracketcove.graphsudoku.domain.IGameRepository
import com.bracketcove.graphsudoku.domain.ISettingsStorage
import com.bracketcove.graphsudoku.domain.Settings
import com.bracketcove.graphsudoku.domain.SettingsStorageResult
import com.bracketcove.graphsudoku.domain.SudokuPuzzle

/**
 * Implementation of [IGameRepository] that coordinates between [IGameDataStorage] and
 * [ISettingsStorage] to fulfill game-related requests from the UI layer.
 */
class GameRepositoryImpl(
    private val gameStorage: IGameDataStorage,
    private val settingsStorage: ISettingsStorage
) : IGameRepository {
    /**
     * Loads the current game from [gameStorage] and re-saves it with the given elapsed time.
     *
     * @param elapsedTime The current time spent on the puzzle.
     * @param onSuccess Callback for successful save.
     * @param onError Callback for errors, including when there is no current game to update.
     */
    override suspend fun saveGame(
        elapsedTime: Long,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val getCurrentGameResult = gameStorage.getCurrentGame()) {
            is GameStorageResult.OnSuccess -> {
                gameStorage.updateGame(
                    getCurrentGameResult.currentGame.copy(
                        elapsedTime = elapsedTime
                    )
                )
                onSuccess(Unit)
            }

            is GameStorageResult.OnError -> {
                onError(getCurrentGameResult.exception)
            }
        }
    }

    /**
     * Persists the given game state directly via [gameStorage].
     *
     * @param game The updated [SudokuPuzzle].
     * @param onSuccess Callback for successful update.
     * @param onError Callback for errors.
     */
    override suspend fun updateGame(
        game: SudokuPuzzle,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val updateGameResult: GameStorageResult = gameStorage.updateGame(game)) {
            is GameStorageResult.OnSuccess -> onSuccess(Unit)
            is GameStorageResult.OnError -> onError(updateGameResult.exception)
        }
    }

    /**
     * Updates a single node's value in [gameStorage] and reports whether the resulting
     * puzzle is complete.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param color The new value.
     * @param elapsedTime The updated time.
     * @param onSuccess Callback indicating if the puzzle is now complete.
     * @param onError Callback for errors.
     */
    override suspend fun updateNode(
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long,
        onSuccess: (isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val result = gameStorage.updateNode(x, y, color, elapsedTime)) {
            is GameStorageResult.OnSuccess -> onSuccess(
                puzzleIsComplete(result.currentGame)
            )

            is GameStorageResult.OnError -> onError(
                result.exception
            )
        }
    }

    /**
     * Retrieves the current game from [gameStorage] and its completion status. If no game
     * currently exists, falls back to reading [settingsStorage] and creating a new game so
     * the caller always receives a valid puzzle.
     *
     * @param onSuccess Callback with the [SudokuPuzzle] and completion status.
     * @param onError Callback for errors.
     */
    /*
    This is mainly where this repository becomes important. The idea for a simple single-player sudoku
    front-end decision maker is that it shouldn't make all of these decisions.
    Hence, adding in an interactor/usecase/transaction script is overkill for an app of this size.
    TODO: add an interactor/usecase/transaction when moving on to multiplayer
    1. request current game
    2a. current game returns onSuccess; forward to caller onSuccess
    2b. current game returns onError
    3b. request current settings from settingsStorage
    4c. settingsStorage returns onSuccess
    4d. settingsStorage returns onError
    5c. write game update to gameStorage (to ensure consistent state between front and back end)
    5d. we're screwed at this point - forward to caller onError
    6e. gameStorage returns onSuccess - forward to caller onSuccess
    6f. gameStorage returns onError - forward to caller onErro
     */
    override suspend fun getCurrentGame(
        onSuccess: (currentGame: SudokuPuzzle, isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val getCurrentGameResult = gameStorage.getCurrentGame()) {
            is GameStorageResult.OnSuccess -> onSuccess(
                getCurrentGameResult.currentGame,
                puzzleIsComplete(
                    getCurrentGameResult.currentGame
                )
            )

            is GameStorageResult.OnError -> {
                when (val getSettingsResult = settingsStorage.getSettings()) {
                    is SettingsStorageResult.OnSuccess -> {
                        when (val updateGameResult =
                            createAndWriteNewGame(getSettingsResult.settings)) {
                            is GameStorageResult.OnSuccess -> onSuccess(
                                updateGameResult.currentGame,
                                puzzleIsComplete(
                                    updateGameResult.currentGame
                                )
                            )

                            is GameStorageResult.OnError -> onError(updateGameResult.exception)
                        }
                    }

                    is SettingsStorageResult.OnError -> onError(getSettingsResult.exception)
                    is SettingsStorageResult.OnComplete -> {} // Should not happen for getSettings
                }
            }
        }
    }

    /**
     * Persists the given settings to [settingsStorage], then builds and writes a fresh
     * Sudoku puzzle based on them via [gameStorage].
     *
     * @param settings The configuration for the new game.
     * @param onSuccess Callback for successful creation.
     * @param onError Callback for errors.
     */
    override suspend fun createNewGame(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val updateSettingsResult = settingsStorage.updateSettings(settings)) {
            is SettingsStorageResult.OnComplete -> {
                when (val updateGameResult = createAndWriteNewGame(settings)) {
                    is GameStorageResult.OnSuccess -> onSuccess(Unit)
                    is GameStorageResult.OnError -> onError(updateGameResult.exception)
                }
            }

            is SettingsStorageResult.OnError -> onError(updateSettingsResult.exception)
            is SettingsStorageResult.OnSuccess -> {
                // Also handle OnSuccess if it's returned by updateSettings
                when (val updateGameResult = createAndWriteNewGame(settings)) {
                    is GameStorageResult.OnSuccess -> onSuccess(Unit)
                    is GameStorageResult.OnError -> onError(updateGameResult.exception)
                }
            }
        }
    }

    /**
     * Builds a brand new [SudokuPuzzle] for the given [settings] and writes it to [gameStorage].
     *
     * @param settings The configuration used to build the new puzzle.
     * @return A [GameStorageResult] indicating success or failure of the write.
     */
    private suspend fun createAndWriteNewGame(settings: Settings): GameStorageResult =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            return@withContext gameStorage.updateGame(
                com.bracketcove.graphsudoku.computationLogic.buildSudoku(
                    settings.boundary,
                    settings.difficulty
                )
            )
        }

    /**
     * Delegates to the computation logic's puzzle completion check.
     *
     * @param puzzle The [SudokuPuzzle] to check.
     * @return True if the puzzle is valid and fully filled in, false otherwise.
     */
    private fun puzzleIsComplete(puzzle: SudokuPuzzle): Boolean {
        return com.bracketcove.graphsudoku.computationLogic.puzzleIsComplete(puzzle)
    }

    /**
     * Retrieves the current user settings from [settingsStorage].
     *
     * @param onSuccess Callback with the [Settings].
     * @param onError Callback for errors.
     */
    override suspend fun getSettings(
        onSuccess: (Settings) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val getSettingsResult = settingsStorage.getSettings()) {
            is SettingsStorageResult.OnSuccess -> onSuccess(getSettingsResult.settings)
            is SettingsStorageResult.OnError -> onError(getSettingsResult.exception)
            is SettingsStorageResult.OnComplete -> {}
        }
    }

    /**
     * Persists the given settings to [settingsStorage].
     *
     * @param settings The new [Settings].
     * @param onSuccess Callback for successful update.
     * @param onError Callback for errors.
     */
    override suspend fun updateSettings(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val updateSettingsResult = settingsStorage.updateSettings(settings)) {
            is SettingsStorageResult.OnComplete -> onSuccess(Unit)
            is SettingsStorageResult.OnSuccess -> onSuccess(Unit)
            is SettingsStorageResult.OnError -> onError(updateSettingsResult.exception)
        }
    }
}
