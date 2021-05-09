package org.jellyfin.client.android.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM Server ORDER BY DisplayOrder")
    fun getAllServers(): Flow<List<DTOServer>>

    @Delete
    fun deleteServer(server: DTOServer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addServer(dtoServer: DTOServer)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateServers(servers: List<DTOServer>)
}