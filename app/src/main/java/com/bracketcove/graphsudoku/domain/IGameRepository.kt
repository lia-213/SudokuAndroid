package com.bracketcove.graphsudoku.domain
/**
 * Repository interface for managing Sudoku games and settings.
 * It abstracts the data storage and provides callback-based methods for UI logic.
 */
interface IGameRepository {
    /**
     * Saves the current game's elapsed time.
     *
     * @param elapsedTime The current time spent on the puzzle.
     * @param onSuccess Callback for successful save.
     * @param onError Callback for errors.
     */
    suspend fun saveGame(
        elapsedTime: Long,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates the current game state.
     *
     * @param game The updated [SudokuPuzzle].
     * @param onSuccess Callback for successful update.
     * @param onError Callback for errors.
     */
    suspend fun updateGame(
        game: SudokuPuzzle,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Creates a new Sudoku game based on the provided settings.
     *
     * @param settings The configuration for the new game.
     * @param onSuccess Callback for successful creation.
     * @param onError Callback for errors.
     */
    suspend fun createNewGame(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates a specific node's value in the current game.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param color The new value.
     * @param elapsedTime The updated time.
     * @param onSuccess Callback indicating if the puzzle is now complete.
     * @param onError Callback for errors.
     */
    suspend fun updateNode(
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long,
        onSuccess: (isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Retrieves the current game state and its completion status.
     *
     * @param onSuccess Callback with the [SudokuPuzzle] and completion status.
     * @param onError Callback for errors.
     */
    suspend fun getCurrentGame(
        onSuccess: (currentGame: SudokuPuzzle, isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Retrieves the current user settings.
     *
     * @param onSuccess Callback with the [Settings].
     * @param onError Callback for errors.
     */
    suspend fun getSettings(
        onSuccess: (Settings) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates the user settings.
     *
     * @param settings The new [Settings].
     * @param onSuccess Callback for successful update.
     * @param onError Callback for errors.
     */
    suspend fun updateSettings(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )
}