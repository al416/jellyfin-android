package org.jellyfin.client.tv.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.tv.MainActivity

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector()
    abstract fun contributesMainActivity(): MainActivity

}