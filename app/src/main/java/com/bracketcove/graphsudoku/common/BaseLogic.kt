package com.bracketcove.graphsudoku.common

import kotlinx.coroutines.Job

// abstract used here for situations where we want to share behaviour (e.g. function stub == abstract function
//which we want to be inherited across any class that inherits from baselogic.kt), and want a protected (not public) variable
abstract class BaseLogic<EVENT> {
    protected lateinit var jobTracker: Job
    abstract fun onEvent(event: EVENT)
}