package com.bracketcove.graphsudoku.ui.activegame

enum class ActiveGameScreenState {
    LOADING,
    ACTIVE,
    COMPLETE
}

// For compatibility with the usage in ViewModel
typealias ActiveGameContentState = ActiveGameScreenState
