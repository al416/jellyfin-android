package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Server

interface LoginRepository {

    suspend fun doUserLogin(baseUrl: String, username: String, password: String): Flow<Resource<Login>>

    suspend fun getServerList(): Flow<Resource<List<Server>>>

    suspend fun updateServers(servers: List<Server>)

    suspend fun addServer(server: Server)

    suspend fun deleteServer(server: Server)
}