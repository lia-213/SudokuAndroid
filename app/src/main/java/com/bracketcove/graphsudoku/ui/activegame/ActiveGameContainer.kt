package com.bracketcove.graphsudoku.ui.activegame

/**
 * Container abstraction for the active game feature. Implemented by the hosting [android.app.Activity]
 * so that [ActiveGameLogic] can trigger platform-specific behavior without depending on Android APIs
 * directly.
 */
interface ActiveGameContainer {
    /**
     * Displays a generic error message to the user.
     */
    fun showError()

    /**
     * Navigates away from the active game to start a new game.
     */
    fun onNewGameClick()
}