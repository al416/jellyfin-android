package org.jellyfin.client.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.android.ui.login.LoginActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector()
    abstract fun contributesLoginActivity(): LoginActivity
}