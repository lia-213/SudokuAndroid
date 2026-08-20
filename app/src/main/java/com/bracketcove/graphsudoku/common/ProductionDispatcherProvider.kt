package com.bracketcove.graphsudoku.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Production implementation of [DispatcherProvider] using [Dispatchers.Main] for UI
 * and [Dispatchers.IO] for background operations.
 */
object ProductionDispatcherProvider : DispatcherProvider {
    /**
     * Returns [Dispatchers.Main].
     */
    override fun provideUIContext(): CoroutineContext {
        return Dispatchers.Main
    }

    /**
     * Returns [Dispatchers.IO].
     */
    override fun provideIOContext(): CoroutineContext {
        // can use Dispatchers.Unconfined in io and ui context for testing in jvm junit testing env.
        return Dispatchers.IO
    }
}