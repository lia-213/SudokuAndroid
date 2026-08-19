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
