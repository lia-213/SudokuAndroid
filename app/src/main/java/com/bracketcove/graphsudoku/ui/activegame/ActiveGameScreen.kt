package com.bracketcove.graphsudoku.ui.activegame

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.bracketcove.graphsudoku.R
import com.bracketcove.graphsudoku.common.toTime
import com.bracketcove.graphsudoku.common.sqrt
import com.bracketcove.graphsudoku.ui.activeGameSubtitle
import com.bracketcove.graphsudoku.ui.components.AppToolbar
import com.bracketcove.graphsudoku.ui.components.LoadingScreen
import com.bracketcove.graphsudoku.ui.inputButton
import com.bracketcove.graphsudoku.ui.mutableSudokuSquare
import com.bracketcove.graphsudoku.ui.newGameSubtitle
import com.bracketcove.graphsudoku.ui.readOnlySudokuSquare
import com.bracketcove.graphsudoku.ui.textColorDark
import com.bracketcove.graphsudoku.ui.textColorLight
import com.bracketcove.graphsudoku.ui.userInputtedNumberDark
import com.bracketcove.graphsudoku.ui.userInputtedNumberLight

/**
 * Root composable for the active game screen. Cross-fades between the loading, active board, and
 * game-complete content based on [viewModel]'s content state, and hosts the toolbar with the
 * "new game" action.
 *
 * @param onEventHandler Handler invoked with [ActiveGameEvent]s produced by user interaction.
 * @param viewModel Supplies board/timer/content state and subscribes to updates from the logic layer.
 */
@OptIn(ExperimentalTransitionApi::class)
@Composable
fun ActiveGameScreen(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel
) {
    val contentTransitionState = remember {
        MutableTransitionState(
            ActiveGameScreenState.LOADING
        )
    }

    viewModel.subContentState = {
        contentTransitionState.targetState = it
    }

    val transition = rememberTransition(contentTransitionState, label = "screenTransition")

    val loadingAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) },
        label = "loadingAlpha"
    ) {
        if (it == ActiveGameScreenState.LOADING) 1f else 0f
    }

    val activeAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) },
        label = "activeAlpha"
    ) {
        if (it == ActiveGameScreenState.ACTIVE) 1f else 0f
    }

    val completeAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) },
        label = "completeAlpha"
    ) {
        if (it == ActiveGameScreenState.COMPLETE) 1f else 0f
    }

    Scaffold(
        topBar = {
            AppToolbar(
                modifier = Modifier.wrapContentHeight(),
                title = stringResource(R.string.app_name)
            ) {
                NewGameIcon(onEventHandler = onEventHandler)
            }
        },
        containerColor = MaterialTheme.colorScheme.primary
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (contentTransitionState.targetState) {
                ActiveGameScreenState.ACTIVE -> Box(
                    Modifier.alpha(activeAlpha)
                ) {
                    GameContent(
                        onEventHandler,
                        viewModel
                    )
                }
                ActiveGameScreenState.LOADING -> Box(
                    Modifier.alpha(loadingAlpha)
                ) {
                    LoadingScreen()
                }

                ActiveGameScreenState.COMPLETE -> Box(
                    Modifier.alpha(completeAlpha)
                ) {
                    GameCompleteContent(
                        viewModel.timerState,
                        viewModel.isNewRecordState
                    )
                }
            }
        }
    }
}

/**
 * Toolbar icon button that dispatches [ActiveGameEvent.OnNewGameClicked] when tapped.
 *
 * @param onEventHandler Handler invoked with the new-game-clicked event.
 */
@Composable
fun NewGameIcon(onEventHandler: (ActiveGameEvent) -> Unit) {
    IconButton(
        onClick = { onEventHandler(ActiveGameEvent.OnNewGameClicked) }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            tint = if (MaterialTheme.colorScheme.primary == Color.White) textColorLight else textColorDark,
            contentDescription = null,
            modifier = Modifier
                .height(36.dp)
        )
    }
}

/**
 * Lays out the active game's board, difficulty stars, timer, and number input row using a
 * [ConstraintLayout], sizing the board to fit the available screen height.
 *
 * @param onEventHandler Handler invoked with events produced by the board and input buttons.
 * @param viewModel Supplies the board state, difficulty, and puzzle boundary.
 */
@Composable
fun GameContent(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val margin = when {
            maxHeight < 500.dp -> 20.dp
            maxHeight < 550.dp -> 8.dp
            else -> 0.dp
        }

        val boardSize = maxWidth - margin

        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (board, timer, diff, inputs) = createRefs()

            Box(
                Modifier
                    .constrainAs(board) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .background(MaterialTheme.colorScheme.surface)
                    .size(boardSize)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
            ) {
                SudokuBoard(
                    onEventHandler,
                    viewModel,
                    boardSize
                )
            }

            Row(
                Modifier
                    .wrapContentSize()
                    .constrainAs(diff) {
                        top.linkTo(board.bottom)
                        end.linkTo(parent.end)
                    }
            ) {
                (0..viewModel.difficulty.ordinal).forEach {
                    Icon(
                        contentDescription = stringResource(R.string.difficulty),
                        imageVector = Icons.Filled.Star,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                }
            }

            Box(
                Modifier
                    .wrapContentSize()
                    .constrainAs(timer) {
                        top.linkTo(board.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 16.dp)
            ) {
                TimerText(viewModel)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .constrainAs(inputs) {
                        top.linkTo(timer.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InputButtonRow(
                    (1..viewModel.boundary).toList(),
                    onEventHandler
                )
            }
        }
    }
}

/**
 * Displays a wrapping row of number input buttons, one for each value the user can enter.
 *
 * @param numbers The values to render as input buttons (typically `1..boundary`).
 * @param onEventHandler Handler invoked with [ActiveGameEvent.OnInput] when a button is tapped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputButtonRow(numbers: List<Int>, onEventHandler: (ActiveGameEvent) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        numbers.forEach {
            SudokuInputButton(
                onEventHandler,
                it
            )
        }
    }

    Spacer(Modifier.size(2.dp))
}

/**
 * A single number input button that dispatches [ActiveGameEvent.OnInput] with [number] when tapped.
 *
 * @param onEventHandler Handler invoked with the input event.
 * @param number The value this button enters when tapped.
 */
@Composable
fun SudokuInputButton(onEventHandler: (ActiveGameEvent) -> Unit,
                      number: Int) {
    TextButton(
        onClick = { onEventHandler.invoke(ActiveGameEvent.OnInput(number))},
        modifier = Modifier
            .requiredSize(56.dp)
            .padding(2.dp),
        border = BorderStroke(
            ButtonDefaults.outlinedButtonBorder.width, MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = number.toString(),
            style = inputButton.copy(color = MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays the elapsed game time, formatted as a clock string, subscribing to timer updates from
 * [viewModel].
 *
 * @param viewModel Supplies the timer state via [ActiveGameViewModel.subTimerState].
 */
@Composable
fun TimerText(viewModel: ActiveGameViewModel) {
    var timerState by remember {
        mutableStateOf("")
    }

    viewModel.subTimerState = {
        timerState = it.toTime()
    }

    Text(
        modifier = Modifier.requiredHeight(36.dp),
        text = timerState,
        style = activeGameSubtitle.copy(
            color = MaterialTheme.colorScheme.secondary
        )
    )
}

/**
 * Renders the Sudoku board as an overlay of tile text fields and grid dividers, subscribing to
 * board state updates from [viewModel].
 *
 * @param onEventHandler Handler invoked with events produced by tapping a tile.
 * @param viewModel Supplies the board state and puzzle boundary.
 * @param size The width/height to render the (square) board at.
 */
@Composable
fun SudokuBoard(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel,
    size: Dp
) {
    val boundary = viewModel.boundary
    val tileOffset = size.value / boundary

    var boardState by remember {
        mutableStateOf(
            viewModel.boardState,
            neverEqualPolicy()
        )
    }

    viewModel.subBoardState = {
        boardState = it
    }

    Box(Modifier.size(size)) {
        SudokuTextFields(
            onEventHandler,
            tileOffset,
            boardState
        )

        BoardGrid(
            boundary,
            tileOffset
        )
    }
}

/**
 * Draws the Sudoku grid lines, using a thicker divider at subgrid boundaries.
 *
 * @param boundary The puzzle's boundary (e.g. 9 for a 9x9 board).
 * @param tileOffset The pixel size (in dp units) of a single tile.
 */
@Composable
fun BoardGrid(boundary: Int, tileOffset: Float) {
    val interval = boundary.sqrt
    (1 until boundary).forEach {
        val width = if (it % interval == 0) 3.dp else 1.dp
        VerticalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .absoluteOffset((tileOffset * it).dp, 0.dp)
                .fillMaxHeight()
                .width(width)
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .absoluteOffset(0.dp, (tileOffset * it).dp)
                .fillMaxWidth()
                .height(width)
        )
    }
}

/**
 * Renders each tile in [boardState] as a positioned text element: editable/clickable tiles for
 * user-entered values, and plain read-only text for pre-filled clues.
 *
 * @param onEventHandler Handler invoked with [ActiveGameEvent.OnTileFocused] when an editable tile
 * is tapped.
 * @param tileOffset The pixel size (in dp units) of a single tile, used for positioning.
 * @param boardState The current value/focus/read-only state of every tile.
 */
@Composable
fun SudokuTextFields(
    onEventHandler: (ActiveGameEvent) -> Unit,
    tileOffset: Float,
    boardState: HashMap<Int, SudokuTile>
) {
    boardState.values.forEach { tile ->
        var text = tile.value.toString()

        if (!tile.readOnly) {
            if (text == "0") text = ""

            Text(
                text = text,
                style = mutableSudokuSquare(tileOffset).copy(
                    color = if (MaterialTheme.colorScheme.primary == Color.White) userInputtedNumberLight
                    else userInputtedNumberDark
                ),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp,
                    )
                    .size(tileOffset.dp)
                    .background(
                        if (tile.hasFocus) MaterialTheme.colorScheme.onPrimary.copy(alpha = .25f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable {
                        onEventHandler.invoke(
                            ActiveGameEvent.OnTileFocused(tile.x, tile.y)
                        )
                    },
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = text,
                style = readOnlySudokuSquare(tileOffset),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp,
                    )
                    .size(tileOffset.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays the game-complete summary: a trophy icon, an optional "new record" message, and the
 * final elapsed time.
 *
 * @param timerState The final elapsed time to display, in seconds.
 * @param isNewRecordState Whether this completion set a new best time for the difficulty/boundary.
 */
@Composable
fun GameCompleteContent(timerState: Long, isNewRecordState: Boolean) {
    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                contentDescription = stringResource(R.string.game_complete),
                imageVector = Icons.Filled.EmojiEvents,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(128.dp)
            )
        }

        if (isNewRecordState) {
            Text(
                text = stringResource(R.string.new_record),
                style = newGameSubtitle.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        Text(
            text = stringResource(R.string.total_time),
            style = newGameSubtitle.copy(
                color = MaterialTheme.colorScheme.secondary
            )
        )

        Text(
            text = timerState.toTime(),
            style = newGameSubtitle.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
