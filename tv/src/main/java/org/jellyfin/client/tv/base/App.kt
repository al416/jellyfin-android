package org.jellyfin.client.tv.base


import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import okhttp3.OkHttpClient
import org.jellyfin.client.tv.di.AppComponent
import org.jellyfin.client.tv.di.AppModule
import org.jellyfin.client.tv.di.DaggerAppComponent

class App : DaggerApplication(), ImageLoaderFactory {

    private fun initDagger(app: App): AppComponent {
        return DaggerAppComponent.builder().appModule(AppModule(app)).build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return initDagger(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this))
                    .build()
            }
            .availableMemoryPercentage(0.5)     // Use 50% of the available memory for image caching
            //.logger(DebugLogger())
            .build()
    }
}