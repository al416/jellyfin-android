package org.jellyfin.client.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.android.ui.home.HomeActivity
import org.jellyfin.client.android.ui.login.LoginActivity
import org.jellyfin.client.android.ui.player.PlayerActivity

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector()
    abstract fun contributesLoginActivity(): LoginActivity

    @ContributesAndroidInjector()
    abstract fun contributesHomeActivity(): HomeActivity

    @ContributesAndroidInjector()
    abstract fun contributesPlayerActivity(): PlayerActivity
}