package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Server

interface LoginRepository {

    suspend fun doUserLogin(server: Server, username: String, password: String): Flow<Resource<Login>>

    suspend fun getServerList(): Flow<Resource<List<Server>>>

    suspend fun addServers(servers: List<Server>)

    suspend fun deleteAllServers()

    suspend fun doUserLogout()
}