package com.bracketcove.graphsudoku.ui.activegame

// a container within which the various parts of an application, or a specific feature of an app, are deployed.

interface ActiveGameContainer {
    fun showError()
    fun onNewGameClick()
}