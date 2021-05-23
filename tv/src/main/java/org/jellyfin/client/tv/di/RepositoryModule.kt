package org.jellyfin.client.tv.di


import dagger.Binds
import dagger.Module
import org.jellyfin.client.android.data.repository.CurrentUserRepositoryImpl
import org.jellyfin.client.android.domain.repository.LoginRepository
import org.jellyfin.client.android.data.repository.LoginRepositoryImpl
import org.jellyfin.client.android.data.repository.MediaRepositoryImpl
import org.jellyfin.client.android.data.repository.ViewsRepositoryImpl
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.MediaRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindsLoginRepository(repo: LoginRepositoryImpl): LoginRepository

    @Singleton
    @Binds
    abstract fun bindsViewsRepository(repo: ViewsRepositoryImpl): ViewsRepository

    @Singleton
    @Binds
    abstract fun bindsMediaRepository(repo: MediaRepositoryImpl): MediaRepository

    @Singleton
    @Binds
    abstract fun bindsCurrentUserRepository(repo: CurrentUserRepositoryImpl): CurrentUserRepository
}