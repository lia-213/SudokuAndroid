package com.bracketcove.graphsudoku.ui.newgame

import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.Settings
import com.bracketcove.graphsudoku.domain.UserStatistics

/**
 * Holds the UI state for the "new game" screen. [NewGameLogic] mutates [settingsState],
 * [statisticsState], and [loadingState] directly; [NewGameScreen] observes them (via
 * Compose's snapshot state) to render the current settings, statistics, and loading
 * transition, subscribing to loading changes through [subLoadingState].
 */
class NewGameViewModel {
    /** The puzzle size/difficulty settings currently selected on screen. */
    internal var settingsState: Settings = Settings(Difficulty.MEDIUM, 9)

    /** The user's historical statistics, displayed on the screen. */
    internal var statisticsState: UserStatistics = UserStatistics(0, 0, 0, 0, 0, 0)

    /**
     * Whether the screen is currently loading. Setting this value invokes
     * [subLoadingState] so the Composable can drive its loading/content transition.
     */
    internal var loadingState: Boolean = true
        set(value) {
            field = value
            subLoadingState?.invoke(field)
        }

    /**
     * Callback set by the Composable to be notified of [loadingState] changes.
     * Assigning it immediately invokes it with the current [loadingState].
     */
    internal var subLoadingState: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(loadingState)
        }
}
