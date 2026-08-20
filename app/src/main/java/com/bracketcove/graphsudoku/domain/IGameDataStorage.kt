package com.bracketcove.graphsudoku.domain


/**
 * Interface for persisting and retrieving Sudoku game data.
 */
interface IGameDataStorage {
    /**
     * Updates the entire game state in storage.
     *
     * @param game The [SudokuPuzzle] to save.
     * @return A [GameStorageResult] indicating success or failure.
     */
    suspend fun updateGame(game:SudokuPuzzle): GameStorageResult

    /**
     * Updates a single node in the current game state.
     *
     * @param x The x coordinate of the node.
     * @param y The y coordinate of the node.
     * @param color The new color (value) of the node.
     * @param elapsedTime The updated elapsed time of the game.
     * @return A [GameStorageResult] indicating success or failure.
     */
    suspend fun updateNode(x: Int, y: Int, color: Int, elapsedTime: Long): GameStorageResult

    /**
     * Retrieves the currently saved game from storage.
     *
     * @return A [GameStorageResult] containing the current game or an error.
     */
    suspend fun getCurrentGame(): GameStorageResult
}

/**
 * Sealed class representing the result of a game storage operation.
 */
sealed class GameStorageResult {
    data class OnSuccess(val currentGame: SudokuPuzzle) : GameStorageResult()
    data class OnError(val exception: Exception) : GameStorageResult()
}