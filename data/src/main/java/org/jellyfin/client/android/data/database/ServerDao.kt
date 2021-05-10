package org.jellyfin.client.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM Server ORDER BY DisplayOrder")
    fun getAllServers(): Flow<List<DTOServer>>

    @Query("DELETE FROM Server")
    fun deleteAllServers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addServers(servers: List<DTOServer>)
}