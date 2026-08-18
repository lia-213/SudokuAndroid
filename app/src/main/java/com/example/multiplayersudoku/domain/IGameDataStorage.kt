package com.example.multiplayersudoku.domain


// result wrapper
interface IGameDataStorage {

}

sealed class GameStorageResult {
    data class OnSuccess(val currentGame: SudokuPuzzle) : GameStorageResult()
    data class OnError(val exception: Exception) : GameStorageResult()
}