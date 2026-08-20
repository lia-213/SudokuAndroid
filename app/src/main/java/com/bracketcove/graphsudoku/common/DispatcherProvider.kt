package com.bracketcove.graphsudoku.common

import kotlin.coroutines.CoroutineContext

/**
 * Interface to provide coroutine contexts for different execution environments (e.g., UI, IO).
 * This allows for easy swapping of dispatchers for testing purposes.
 */
interface DispatcherProvider {
    /**
     * Returns the [CoroutineContext] for UI-related tasks.
     */
    fun provideUIContext(): CoroutineContext

    /**
     * Returns the [CoroutineContext] for background/IO tasks.
     */
    fun provideIOContext(): CoroutineContext
}