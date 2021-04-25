package org.jellyfin.client.android.di

import dagger.Binds
import dagger.Module
import org.jellyfin.client.android.domain.repository.LoginRepository
import org.jellyfin.client.android.data.repository.LoginRepositoryImpl
import org.jellyfin.client.android.data.repository.ViewsRepositoryImpl
import org.jellyfin.client.android.domain.repository.ViewsRepository

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindsLoginRepository(repo: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindsViewsRepository(repo: ViewsRepositoryImpl): ViewsRepository
}