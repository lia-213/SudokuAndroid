package com.bracketcove.graphsudoku.ui.newgame

import com.bracketcove.graphsudoku.domain.Difficulty

/**
 * UI events emitted by the "new game" screen and handled by [NewGameLogic].
 */
sealed class NewGameEvent {
    /** The screen has started; existing settings and statistics should be loaded. */
    object OnStart: NewGameEvent()

    /** The user changed the puzzle size/boundary via the size dropdown. */
    data class OnSizeChanged(val boundary: Int): NewGameEvent()

    /** The user changed the puzzle difficulty via the difficulty dropdown. */
    data class OnDifficultyChanged(val diff: Difficulty): NewGameEvent()

    /** The user pressed "done", requesting that a new game be created with the current settings. */
    object OnDonePressed: NewGameEvent()
}