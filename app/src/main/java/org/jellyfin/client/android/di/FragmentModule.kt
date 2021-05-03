package org.jellyfin.client.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.android.ui.home.HomeFragment
import org.jellyfin.client.android.ui.home.RecentItemFragment
import org.jellyfin.client.android.ui.login.LoginFragment
import org.jellyfin.client.android.ui.login.add_server.AddServerFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributesLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributesHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributesRecentItemFragment(): RecentItemFragment

    @ContributesAndroidInjector
    abstract fun contributesAddServerFragment(): AddServerFragment
}