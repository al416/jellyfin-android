package org.jellyfin.client.android.data.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Session

@Dao
interface SessionDao {

    @Query("SELECT SessionId as sessionId, Session.ServerId as serverId, Server.URL as serverUrl, " +
            "Session.UserName as userName, Session.UserUUID as userUUID, Session.APIKey as apiKey " +
            "FROM Session " +
            "INNER JOIN Server ON Session.ServerId = Server.ServerId " +
            "WHERE SessionId = 1")
    fun getCurrentSession(): Flow<Session>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setCurrentSession(dtoSession: DTOSession)

    @Query("DELETE FROM Session where SessionId = 1")
    fun deleteCurrentSession()
}