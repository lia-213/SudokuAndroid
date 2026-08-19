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

class LocalGameStorageImpl(
    fileStorageDirectory: String,
    private val pathToStorageFile: File = File(fileStorageDirectory, FILE_NAME)
) : IGameDataStorage {

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
