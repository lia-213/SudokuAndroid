package com.bracketcove.graphsudoku.ui.activegame

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bracketcove.graphsudoku.R
import com.bracketcove.graphsudoku.common.makeToast
import com.bracketcove.graphsudoku.ui.GraphSudokuTheme
import com.bracketcove.graphsudoku.ui.activegame.buildlogic.buildActiveGameLogic
import com.bracketcove.graphsudoku.ui.newgame.NewGameActivity

// purpose: as a feature-specific container
class ActiveGameActivity : ComponentActivity(), ActiveGameContainer {
    // creating a reference to ActiveGameLogic class
    private lateinit var logic: ActiveGameLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ActiveGameViewModel()

        logic = buildActiveGameLogic(this, viewModel, applicationContext)

        setContent {
            GraphSudokuTheme {
                ActiveGameScreen(
                    // pass in a function type that will serve as event handler
                    onEventHandler = logic::onEvent,
                    viewModel
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logic.onEvent(ActiveGameEvent.OnStart)
    }

    override fun onStop() {
        super.onStop()
        logic.onEvent(ActiveGameEvent.OnStop)
    }

    override fun showError() = makeToast(getString(R.string.generic_error))

    override fun onNewGameClick() {
        startActivity(
            Intent(
                this,
                NewGameActivity::class.java
            )
        )
    }
}
