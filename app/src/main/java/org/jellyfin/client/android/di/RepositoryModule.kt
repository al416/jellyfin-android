package org.jellyfin.client.android.di

import dagger.Binds
import dagger.Module
import org.jellyfin.client.android.repository.LoginRepository
import org.jellyfin.client.android.repository.LoginRepositoryImpl
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindsLoginRepository(repo: LoginRepositoryImpl): LoginRepository
}