package com.bracketcove.graphsudoku.common

import kotlinx.coroutines.Job

/**
 * Base class for UI Logic (or ViewModels in other architectures) that handles UI events.
 * It provides a [jobTracker] to manage coroutines and an [onEvent] function to be implemented
 * by subclasses to process specific UI events.
 *
 * @param EVENT The type of event that this logic class can handle.
 */
abstract class BaseLogic<EVENT> {
    protected lateinit var jobTracker: Job

    /**
     * Handles the incoming [event] from the UI.
     *
     * @param event The event to be processed.
     */
    abstract fun onEvent(event: EVENT)
}