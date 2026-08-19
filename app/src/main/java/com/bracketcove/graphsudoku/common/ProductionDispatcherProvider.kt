package com.bracketcove.graphsudoku.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

// objects are singletons - only one ever in memory at one particular time; threadsafe (even though co-r not a thread); can inherit from an interface
object ProductionDispatcherProvider : DispatcherProvider {
    override fun provideUIContext(): CoroutineContext {
        return Dispatchers.Main
    }

    override fun provideIOContext(): CoroutineContext {
        // can use Dispatchers.Unconfined in io and ui context for testing in jvm junit testing env.
        return Dispatchers.IO
    }
}