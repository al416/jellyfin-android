package org.jellyfin.client.android.data.database


import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM Session where SessionId = 1")
    fun getCurrentSession(): Flow<DTOSession>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun setCurrentSession(dtoSession: DTOSession)
}