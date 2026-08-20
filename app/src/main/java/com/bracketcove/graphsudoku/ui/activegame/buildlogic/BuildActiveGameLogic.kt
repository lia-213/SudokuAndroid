package com.bracketcove.graphsudoku.ui.activegame.buildlogic

import android.content.Context
import com.bracketcove.graphsudoku.common.ProductionDispatcherProvider
import com.bracketcove.graphsudoku.persistence.GameRepositoryImpl
import com.bracketcove.graphsudoku.persistence.LocalGameStorageImpl
import com.bracketcove.graphsudoku.persistence.LocalSettingsStorageImpl
import com.bracketcove.graphsudoku.persistence.LocalStatisticsStorageImpl
import com.bracketcove.graphsudoku.persistence.settingsDataStore
import com.bracketcove.graphsudoku.persistence.statsDataStore
import com.bracketcove.graphsudoku.ui.activegame.ActiveGameContainer
import com.bracketcove.graphsudoku.ui.activegame.ActiveGameLogic
import com.bracketcove.graphsudoku.ui.activegame.ActiveGameViewModel

/**
 * Assembles a fully-wired [ActiveGameLogic] instance, constructing its concrete repository and
 * dispatcher dependencies (backed by local file/DataStore storage) for production use.
 *
 * @param container The [ActiveGameContainer] (typically the hosting Activity) that handles
 * navigation and error display.
 * @param viewModel The [ActiveGameViewModel] that will receive board/timer/state updates.
 * @param context The Android [Context] used to resolve the app's file/DataStore locations.
 * @return A ready-to-use [ActiveGameLogic].
 */
internal fun buildActiveGameLogic(
    container: ActiveGameContainer,
    viewModel: ActiveGameViewModel,
    context: Context
): ActiveGameLogic {
    return ActiveGameLogic(
        container,
        viewModel,
        GameRepositoryImpl(
            // how to get the path to the storage directory sued for this application
            LocalGameStorageImpl(context.filesDir.path),
            LocalSettingsStorageImpl(context.settingsDataStore)
        ),
        LocalStatisticsStorageImpl(
            context.statsDataStore
        ),
        ProductionDispatcherProvider
    )
}
