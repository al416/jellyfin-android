package org.jellyfin.client.android.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.jellyfin.client.android.base.App
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityModule::class,
        FragmentModule::class]
)
interface AppComponent : AndroidInjector<App> {
    override fun inject(app: App)
}