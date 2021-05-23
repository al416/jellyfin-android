package org.jellyfin.client.tv.di


import android.content.Context
import dagger.Module
import dagger.Provides
import org.jellyfin.client.android.data.database.JellyfinDatabase
import org.jellyfin.client.android.data.database.ServerDao
import org.jellyfin.client.android.data.database.SessionDao

@Module
interface DatabaseModule {

    companion object {

        @Provides
        fun providesJellyfinDatabase(context: Context): JellyfinDatabase {
            return JellyfinDatabase.getDatabase(context)
        }

        @Provides
        fun providesSessionDao(database: JellyfinDatabase): SessionDao {
            return database.sessionDao()
        }

        @Provides
        fun providesServerDao(database: JellyfinDatabase): ServerDao {
            return database.serverDao()
        }

    }

}