package com.bracketcove.graphsudoku.persistence

import com.bracketcove.graphsudoku.domain.GameStorageResult
import com.bracketcove.graphsudoku.domain.IGameDataStorage
import com.bracketcove.graphsudoku.domain.IGameRepository
import com.bracketcove.graphsudoku.domain.ISettingsStorage
import com.bracketcove.graphsudoku.domain.Settings
import com.bracketcove.graphsudoku.domain.SettingsStorageResult
import com.bracketcove.graphsudoku.domain.SudokuPuzzle

// focusing on extracting data from different backend data sources depending on the situation so presentation class m
class GameRepositoryImpl (
    private val gameStorage: IGameDataStorage,
    private val settingsStorage: ISettingsStorage
) : IGameRepository {
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
            // when the user opens the app and starts new game
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
                }
            }
        }
    }

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
        }
    }

    private suspend fun createAndWriteNewGame(settings: Settings): GameStorageResult {
        return gameStorage.updateGame(
            SudokuPuzzle(
                settings.boundary,
                settings.difficulty
            )
        )
    }

    override suspend fun getSettings(
        onSuccess: (Settings) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when (val getSettingsResult = settingsStorage.getSettings()) {
            is SettingsStorageResult.OnSuccess -> onSuccess(getSettingsResult.settings)
            is SettingsStorageResult.OnError -> onError(getSettingsResult.exception)

        }
    }

    override suspend fun updateSettings(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // TODO: eventually wrap this in a when block to handle errors like in other methods for this class
        settingsStorage.updateSettings(settings)
        onSuccess(Unit)
    }

}