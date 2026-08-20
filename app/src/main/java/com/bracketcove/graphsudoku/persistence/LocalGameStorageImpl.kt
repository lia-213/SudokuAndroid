package com.bracketcove.graphsudoku.persistence

import com.bracketcove.graphsudoku.domain.GameStorageResult
import com.bracketcove.graphsudoku.domain.IGameDataStorage
import com.bracketcove.graphsudoku.domain.SudokuPuzzle
import com.bracketcove.graphsudoku.domain.getHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private const val FILE_NAME = "game_state.txt"

/**
 * Implementation of [IGameDataStorage] that serializes the [SudokuPuzzle] to a flat file
 * on disk via Java object serialization, rather than a database.
 */
class LocalGameStorageImpl(
    fileStorageDirectory: String,
    private val pathToStorageFile: File = File(fileStorageDirectory, FILE_NAME)
) : IGameDataStorage {

    /**
     * Serializes and writes the entire game state to [pathToStorageFile].
     *
     * @param game The [SudokuPuzzle] to save.
     * @return A [GameStorageResult] indicating success or failure.
     */
    override suspend fun updateGame(game: SudokuPuzzle): GameStorageResult {
        return withContext(Dispatchers.IO) {
            val result: GameStorageResult = try {
                updateGameData(game)
                GameStorageResult.OnSuccess(game)
            } catch (e: Exception) {
                GameStorageResult.OnError(e)
            }
            result
        }
    }

    /**
     * Writes the given [game] to [pathToStorageFile] using [ObjectOutputStream].
     *
     * @param game The [SudokuPuzzle] to serialize to disk.
     */
    private fun updateGameData(game: SudokuPuzzle) {
        try {
            val fileOutputStream = FileOutputStream(pathToStorageFile)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(game)
            objectOutputStream.close()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Reads the current game from disk, updates a single node's color (unless it is
     * read-only), updates the elapsed time, and re-serializes the game back to
     * [pathToStorageFile].
     *
     * @param x The x coordinate of the node.
     * @param y The y coordinate of the node.
     * @param color The new color (value) of the node.
     * @param elapsedTime The updated elapsed time of the game.
     * @return A [GameStorageResult] indicating success or failure.
     */
    override suspend fun updateNode(
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long
    ): GameStorageResult {
        return withContext(Dispatchers.IO) {
            val result: GameStorageResult = try {
                val game = getGame()

                game.graph[getHash(x, y)]?.firstOrNull()?.let {
                    if (!it.readOnly) it.color = color
                }
                game.elapsedTime = elapsedTime
                updateGameData(game)
                GameStorageResult.OnSuccess(game)
            } catch (e: Exception) {
                GameStorageResult.OnError(e)
            }
            result
        }
    }

    /**
     * Reads and deserializes the [SudokuPuzzle] currently stored at [pathToStorageFile].
     *
     * @return The deserialized [SudokuPuzzle].
     */
    private fun getGame(): SudokuPuzzle {
        try {
            val fileInputStream = FileInputStream(pathToStorageFile)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val game = objectInputStream.readObject() as SudokuPuzzle
            objectInputStream.close()
            return game
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Retrieves the currently saved game from [pathToStorageFile].
     *
     * @return A [GameStorageResult] containing the current game or an error (e.g. if the
     * file does not exist yet).
     */
    override suspend fun getCurrentGame(): GameStorageResult {
        return withContext(Dispatchers.IO) {
            val result: GameStorageResult = try {
                GameStorageResult.OnSuccess(getGame())
            } catch (e: Exception) {
                GameStorageResult.OnError(e)
            }
            result
        }
    }
}
