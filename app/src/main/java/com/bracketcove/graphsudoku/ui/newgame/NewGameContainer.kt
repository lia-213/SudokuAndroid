package com.bracketcove.graphsudoku.ui.newgame

/**
 * Callback surface implemented by the host (e.g. [NewGameActivity]) so that
 * [NewGameLogic] can trigger UI-level effects such as error messages and navigation
 * without depending on Android framework classes directly.
 */
interface NewGameContainer {
    /** Notifies the host that an error occurred and should be surfaced to the user. */
    fun showError()

    /** Notifies the host that the new game was created successfully and setup is complete. */
    fun onDoneClick()
}