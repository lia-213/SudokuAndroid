package com.bracketcove.graphsudoku.ui.newgame

import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.Settings
import com.bracketcove.graphsudoku.domain.UserStatistics

class NewGameViewModel {
    internal var settingsState: Settings = Settings(Difficulty.MEDIUM, 9)
    internal var statisticsState: UserStatistics = UserStatistics(0, 0, 0, 0, 0, 0)
    
    internal var loadingState: Boolean = true
        set(value) {
            field = value
            subLoadingState?.invoke(field)
        }

    internal var subLoadingState: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(loadingState)
        }
}
