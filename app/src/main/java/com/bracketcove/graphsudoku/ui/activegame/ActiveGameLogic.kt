package com.bracketcove.graphsudoku.ui.activegame

import com.bracketcove.graphsudoku.common.BaseLogic
import com.bracketcove.graphsudoku.common.DispatcherProvider
import com.bracketcove.graphsudoku.domain.IGameRepository
import com.bracketcove.graphsudoku.domain.IStatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

// presentation logic represeents the presentation logic of this particular feature of the app\
// coordinates between teh container, the viewmodel (and by extension, the view) and the backend of the app
class ActiveGameLogic(
    private val container: ActiveGameContainer?,
    private val viewModel: ActiveGameViewModel,
    private val gameRepo: IGameRepository,
    private val statsRepo: IStatisticsRepository,
    private val dispatcher: DispatcherProvider
) : BaseLogic<ActiveGameEvent>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() =  dispatcher.provideUIContext() + jobTracker

    init {
        jobTracker = Job()
    }

    // android-specific timer classes introduced diff problems in implementation
    // inline: copy-paste
    // crossinline function type:
    // ----- in the lambda function, we can't wrtie a recurrent statement. taking a preventative step  for unexpected behaviour
    inline fun startCoroutineTime(
        crossinline action: () -> Unit
    ) = launch {
        while(true) {
            action()
            // delaying the coroutine doesn't block the thread we are on!
            delay(1000)
        }
    }

    // how to sstop the above coroutine
    private var timerTracker: Job? = null

    private val Long.timeOffset: Long
        get() {
            return if (this <= 0) 0
            else this -1
        }

    override fun onEvent(event: ActiveGameEvent) {
        TODO("Not yet implemented")
    }

}