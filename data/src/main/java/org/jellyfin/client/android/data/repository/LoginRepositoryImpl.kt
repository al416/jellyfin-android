package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.jellyfin.client.android.data.database.DTOServer
import org.jellyfin.client.android.data.database.DTOSession
import org.jellyfin.client.android.data.database.ServerDao
import org.jellyfin.client.android.data.database.SessionDao
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.client.KtorClient
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.model.api.AuthenticateUserByName
import javax.inject.Inject
import javax.inject.Named

class LoginRepositoryImpl @Inject constructor(@Named("network") private val networkDispatcher: CoroutineDispatcher,
                                              @Named("disk") private val diskDispatcher: CoroutineDispatcher,
                                              private val api: KtorClient,
                                              private val userApi: UserApi,
                                              private val serverDao: ServerDao,
                                              private val sessionDao: SessionDao,
                                              private val viewsRepository: ViewsRepository
) : LoginRepository {

    override suspend fun doUserLogin(server: Server, username: String, password: String): Flow<Resource<Login>> {
        return flow<Resource<Login>> {
            emit(Resource.loading())
            api.baseUrl = server.url
            // TODO: Why is there pw and password? Should both be set?
            val user = AuthenticateUserByName(username = username, password = password, pw = password)
            try {
                val authenticationResult by userApi.authenticateUserByName(user)
                val token = authenticationResult.accessToken ?: ""
                api.accessToken = token
                val userUUID = authenticationResult.user?.id.toString() ?: ""
                sessionDao.deleteCurrentSession()
                sessionDao.setCurrentSession(DTOSession(sessionId = 1, serverId = server.id, userUUID = userUUID,
                    userName = username, apiKey = token))
                emit(Resource.success(Login(accessToken = token)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                emit(Resource.error(listOf(Error(httpErrorResponseCode = null, code = 1, message = e.message, exception = null))))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getServerList(): Flow<Resource<List<Server>>> {
        return serverDao.getAllServers().map {serverList ->
            Resource.success(serverList.map {dtoServer ->
                Server(dtoServer.serverId, dtoServer.serverName, dtoServer.serverUrl, dtoServer.displayOrder)
            })
        }.flowOn(diskDispatcher)
    }

    override suspend fun addServers(servers: List<Server>) {
        serverDao.addServers(servers.map {
            DTOServer(serverId = it.id,
                serverName = it.name,
                serverUrl = it.url,
                displayOrder = it.displayOrder)
        })
    }

    override suspend fun deleteAllServers() {
        serverDao.deleteAllServers()
    }

    override suspend fun doUserLogout() {
        sessionDao.deleteCurrentSession()
        viewsRepository.clearCache()
        api.baseUrl = null
        api.accessToken = null
    }

}