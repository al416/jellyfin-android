package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.repository.LoginRepository
import org.jellyfin.sdk.api.client.KtorClient
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.model.api.AuthenticateUserByName
import javax.inject.Inject
import javax.inject.Named

class LoginRepositoryImpl @Inject constructor(@Named("computation") private val computationDispatcher: CoroutineDispatcher,
                                              private val api: KtorClient,
                                              private val userApi: UserApi
) : LoginRepository {

    override suspend fun doUserLogin(baseUrl: String, username: String, password: String): Flow<Resource<Login>> {
        return flow<Resource<Login>> {
            emit(Resource.loading())
            api.baseUrl = baseUrl
            // TODO: Why is there pw and password? Should both be set?
            val user = AuthenticateUserByName(username = username, password = password, pw = password)
            try {
                val authenticationResult by userApi.authenticateUserByName(user)
                api.accessToken = authenticationResult.accessToken
                emit(Resource.success(Login(authenticationResult.accessToken)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                emit(Resource.error(listOf(Error(httpErrorResponseCode = null, code = 1, message = e.message, exception = null))))
            }
        }.flowOn(computationDispatcher)
    }

}