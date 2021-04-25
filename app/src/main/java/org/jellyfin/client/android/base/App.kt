package org.jellyfin.client.android.base

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import org.jellyfin.client.android.di.AppComponent
import org.jellyfin.client.android.di.AppModule
import org.jellyfin.client.android.di.DaggerAppComponent

class App : DaggerApplication() {

    private fun initDagger(app: App): AppComponent {
        return DaggerAppComponent.builder().appModule(AppModule(app)).build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = initDagger(this)

}