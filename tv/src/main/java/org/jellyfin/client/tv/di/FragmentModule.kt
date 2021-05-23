package org.jellyfin.client.tv.di


import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.tv.MainFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributesMainFragment(): MainFragment
}