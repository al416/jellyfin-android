package org.jellyfin.client.android.di

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
@Suppress("unused")
class ProviderModule {

    @Provides
    internal fun providesExoPlayer(context: Context): ExoPlayer {
        return SimpleExoPlayer.Builder(context).build()
    }
}