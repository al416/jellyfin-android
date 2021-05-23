package org.jellyfin.client.tv.di


import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.tv.base.App
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ActivityModule::class,
        AppModule::class,
        FragmentModule::class,
        ThreadModule::class,
        JellyfinModule::class,
        RepositoryModule::class,
        ViewModelModule::class,
        //ProviderModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent : AndroidInjector<App> {
    override fun inject(app: App)
}