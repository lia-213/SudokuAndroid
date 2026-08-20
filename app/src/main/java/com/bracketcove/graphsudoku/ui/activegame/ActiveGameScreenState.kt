package com.bracketcove.graphsudoku.ui.activegame

/**
 * Represents the high-level content state of the active game screen, used to drive the
 * cross-fade transition between the loading, active board, and completion views.
 */
enum class ActiveGameScreenState {
    /** The current game is being loaded and a loading indicator should be shown. */
    LOADING,

    /** A game is in progress and the Sudoku board and inputs should be shown. */
    ACTIVE,

    /** The current game has been completed and the completion summary should be shown. */
    COMPLETE
}

// For compatibility with the usage in ViewModel
typealias ActiveGameContentState = ActiveGameScreenState
