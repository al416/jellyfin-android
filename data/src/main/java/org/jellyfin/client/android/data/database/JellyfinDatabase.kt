package org.jellyfin.client.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DTOServer::class, DTOSession::class],
    version = 1,
    exportSchema = false
)
abstract class JellyfinDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerDao

    abstract fun sessionDao(): SessionDao

    companion object {

        @Volatile
        private var INSTANCE: JellyfinDatabase? = null

        fun getDatabase(context: Context): JellyfinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JellyfinDatabase::class.java,
                    "database.db")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}