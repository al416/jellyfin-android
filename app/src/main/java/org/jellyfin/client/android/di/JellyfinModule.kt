package org.jellyfin.client.android.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.KtorClient
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.api.operations.UserLibraryApi
import org.jellyfin.sdk.api.operations.UserViewsApi
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo
import java.util.*

@Module
@Suppress("unused")
object JellyfinModule {

    @Provides
    @Reusable
    internal fun provideJellyfin(): Jellyfin {
        return Jellyfin {
            // TODO: Set correct client and device info
            clientInfo = ClientInfo(name = "My awesome client!", version = "1.33.7")
            deviceInfo = DeviceInfo(id = UUID.randomUUID().toString(), name = "Awesome device")
        }
    }

    @Provides
    @Reusable
    internal fun provideKtorClient(jellyfin: Jellyfin): KtorClient {
        return jellyfin.createApi(baseUrl = null)
    }

    @Provides
    @Reusable
    internal fun providesUserApi(api: KtorClient): UserApi {
        return UserApi(api)
    }

    @Provides
    @Reusable
    internal fun providesUserViewsApi(api: KtorClient): UserViewsApi {
        return UserViewsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesItemsApi(api: KtorClient): ItemsApi {
        return ItemsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesTvShowsApi(api: KtorClient): TvShowsApi {
        return TvShowsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesUserLibraryApi(api: KtorClient): UserLibraryApi {
        return UserLibraryApi(api)
    }

    @Provides
    @Reusable
    internal fun providesImageApi(api: KtorClient): ImageApi {
        return ImageApi(api)
    }

}