package com.bracketcove.graphsudoku.domain

// suspend keyword used bc these functionswill be called from coroutine scope,
// which exist in the logic class/presenter that will be referencing this particular interface.

/* a suspend function is one that can be paused and resumed without blocking the thread it's running on.
When you call a suspend function and it needs to wait for something (a network call, a database read, a timer),
, it doesn't block the thread (I/O); instead, it hands control back to the thread so other work can happen, then picks up where it left off when the result is ready.
kind of like async def with await in python:
    async def fetch_game_state():
        result = await firebase.get("games/abc") --- pauses here without blocking
        return result

which would map to:
    suspend fun fetchGameState(): GameState {
        return firestore.document("games/abc").get().await()
    }

my initial thoughts were it maps to a generator in python using the yield keyword, which also allows
a function to pause mid-execution and hand control back to the caller, then resume from exactly where
they left off BUT a python generator pauses to produce a value for the caller to consume, one at a time.
Think: lazy sequences.
*/

// Unit is equivalent to java's void, meaning the function doesn't return a meaningful value
// BUT: Unit is an actual type with a single value (value == Unit), whereas Java's void is not a type at all
// This matters in Kotlin bc everything is an expression - if there's nothing meaningful to reutrn, it returns the
// Unit object

/* fun doSomething(): Unit {  // explicit
    println("hello")
}

fun doSomething() {  // implicit - Kotlin infers Unit, this is the normal way
    println("hello")
}
*/
// whenever you see a function returning Unit, or no return type at all, read it as:
// "this function does something but gives you nothing back." same as void

// updateGame will write a whole new sudoku puzzle
interface IGameRepository {
    suspend fun saveGame(
        elapsedTime: Long,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun updateGame(
        game: SudokuPuzzle,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun createNewGame(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun updateNode(
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long,
        onSuccess: (isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun getCurrentGame(
        onSuccess: (currentGame: SudokuPuzzle, isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun getSettings(
        onSuccess: (Settings) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun updateSettings(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    )
}