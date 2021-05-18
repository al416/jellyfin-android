package org.jellyfin.client.android.di

import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@Module
@Suppress("unused")
interface ThreadModule {
    companion object {
        @Singleton
        @Provides
        @Named("network_executor")
        fun providesNetworkExecutor(): Executor {
            return Executors.newFixedThreadPool(6)
        }

        @Singleton
        @Provides
        @Named("disk_executor")
        fun providesDiskExecutor(): Executor {
            return Executors.newSingleThreadExecutor()
        }

        @Singleton
        @Provides
        @Named("ui_executor")
        fun providesUiExecutor(): Executor {
            return MainThreadExecutor()
        }

        @Singleton
        @Provides
        @Named("network")
        fun providesNetworkDispatcher(@Named("network_executor") executor: Executor): CoroutineDispatcher {
            return executor.asCoroutineDispatcher()
        }

        @Singleton
        @Provides
        @Named("computation")
        fun providesComputationDispatcher(): CoroutineDispatcher {
            return newSingleThreadContext("Computation")
        }

        @Singleton
        @Provides
        @Named("disk")
        fun providesDiskDispatcher(@Named("disk_executor") executor: Executor): CoroutineDispatcher {
            return executor.asCoroutineDispatcher()
        }
    }
}

private class MainThreadExecutor : Executor {
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    override fun execute(command: Runnable) {
        mainThreadHandler.post(command)
    }
}
