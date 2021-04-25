package org.jellyfin.client.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.android.ui.login.LoginFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributesLoginFragment(): LoginFragment
}