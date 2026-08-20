package com.bracketcove.graphsudoku.ui.newgame.buildlogic

import android.content.Context
import com.bracketcove.graphsudoku.common.ProductionDispatcherProvider
import com.bracketcove.graphsudoku.persistence.*
import com.bracketcove.graphsudoku.ui.newgame.NewGameContainer
import com.bracketcove.graphsudoku.ui.newgame.NewGameLogic
import com.bracketcove.graphsudoku.ui.newgame.NewGameViewModel

/**
 * Assembles a [NewGameLogic] instance with its production dependencies: repositories
 * backed by local file/DataStore persistence and the production [DispatcherProvider].
 *
 * @param container The UI host that will receive navigation/error callbacks.
 * @param viewModel The screen's view model to be mutated by the logic.
 * @param context Android [Context] used to locate local storage locations.
 * @return A fully-wired [NewGameLogic] instance.
 */
internal fun buildNewGameLogic(
    container: NewGameContainer,
    viewModel: NewGameViewModel,
    context: Context
): NewGameLogic {
    return NewGameLogic(
        container,
        viewModel,
        GameRepositoryImpl(
            LocalGameStorageImpl(context.filesDir.path),
            LocalSettingsStorageImpl(context.settingsDataStore)
        ),
        LocalStatisticsStorageImpl(
            context.statsDataStore
        ),
        ProductionDispatcherProvider
    )
}