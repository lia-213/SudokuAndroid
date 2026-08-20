package com.bracketcove.graphsudoku.ui.activegame

/**
 * Represents user interactions on the active game screen, passed to [ActiveGameLogic.onEvent]
 * for handling.
 */
sealed class ActiveGameEvent {
    /**
     * The user entered a number using one of the input buttons.
     *
     * @param input The value entered by the user.
     */
    data class OnInput(val input: Int): ActiveGameEvent()

    /**
     * The user tapped a tile on the board, giving it input focus.
     *
     * @param x The tile's x coordinate.
     * @param y The tile's y coordinate.
     */
    data class OnTileFocused(val x: Int, val y: Int): ActiveGameEvent()

    /**
     * The user tapped the "new game" icon.
     */
    object OnNewGameClicked : ActiveGameEvent()

    /**
     * The screen has started (or resumed), signaling that the current game should be loaded.
     */
    object OnStart : ActiveGameEvent()

    /**
     * The screen has stopped, signaling that the current game progress should be saved.
     */
    object OnStop : ActiveGameEvent()
}