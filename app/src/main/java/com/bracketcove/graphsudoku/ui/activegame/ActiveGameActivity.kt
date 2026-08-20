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

/**
 * Hosts the "active game" feature screen. Wires together [ActiveGameViewModel], [ActiveGameLogic]
 * and the Compose UI ([ActiveGameScreen]), and implements [ActiveGameContainer] so the presentation
 * logic can trigger Android-specific behavior (showing toasts, navigating to a new game).
 */
class ActiveGameActivity : ComponentActivity(), ActiveGameContainer {
    // creating a reference to ActiveGameLogic class
    private lateinit var logic: ActiveGameLogic

    /**
     * Builds the [ActiveGameLogic] instance and sets the Compose content for the screen.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
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

    /**
     * Notifies [logic] that the screen has started, triggering the game to be loaded and the
     * timer to resume.
     */
    override fun onStart() {
        super.onStart()
        logic.onEvent(ActiveGameEvent.OnStart)
    }

    /**
     * Notifies [logic] that the screen has stopped, triggering the current game progress to be
     * saved.
     */
    override fun onStop() {
        super.onStop()
        logic.onEvent(ActiveGameEvent.OnStop)
    }

    /**
     * Displays a generic error toast to the user.
     */
    override fun showError() = makeToast(getString(R.string.generic_error))

    /**
     * Navigates to [NewGameActivity] so the user can configure and start a new puzzle.
     */
    override fun onNewGameClick() {
        startActivity(
            Intent(
                this,
                NewGameActivity::class.java
            )
        )
    }
}
