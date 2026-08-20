package com.bracketcove.graphsudoku.ui.newgame

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bracketcove.graphsudoku.R
import com.bracketcove.graphsudoku.common.makeToast
import com.bracketcove.graphsudoku.ui.GraphSudokuTheme
import com.bracketcove.graphsudoku.ui.activegame.ActiveGameActivity
import com.bracketcove.graphsudoku.ui.newgame.buildlogic.buildNewGameLogic

/**
 * Hosts the "new game" setup screen. Wires the Compose UI ([NewGameScreen]) to
 * [NewGameLogic] and [NewGameViewModel], and implements [NewGameContainer] to receive
 * navigation and error callbacks from the logic layer.
 *
 * This feature is so simple that it is not even worth it to have a separate logic class
 */
class NewGameActivity : ComponentActivity(), NewGameContainer {
    private lateinit var logic: NewGameLogic


    /**
     * Builds the [NewGameLogic] instance and renders [NewGameScreen] as the activity's content.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = NewGameViewModel()

        logic = buildNewGameLogic(this, viewModel, applicationContext)

        // The other feature should be completely restarted each time back is pressed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActiveGameActivity()
            }
        })

        setContent {
            GraphSudokuTheme {
                NewGameScreen(
                    onEventHandler = logic::onEvent,
                    viewModel
                )
            }
        }

    }

    /**
     * Notifies [logic] that the screen has started, triggering the initial load of
     * settings and statistics.
     */
    override fun onStart() {
        super.onStart()
        logic.onEvent(NewGameEvent.OnStart)
    }

    /** Shows a generic error toast to the user. */
    override fun showError() = makeToast(getString(R.string.generic_error))

    /** Called once a new game has been created; navigates to [ActiveGameActivity]. */
    override fun onDoneClick() {
        startActiveGameActivity()
    }

    /**
     * Starts [ActiveGameActivity] as a fresh task, clearing this activity off the back stack.
     */
    private fun startActiveGameActivity() {
        startActivity(
            Intent(
                this,
                ActiveGameActivity::class.java
            ).apply {
                this.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }

}
