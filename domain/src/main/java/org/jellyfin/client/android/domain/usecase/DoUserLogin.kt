package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class DoUserLogin @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                      private val loginRepository: LoginRepository
) : BaseUseCase<Login, DoUserLogin.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<Login>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return loginRepository.doUserLogin(baseUrl = params.baseUrl, username = params.username, password = params.password)
    }

    data class RequestParams(val baseUrl: String, val username: String, val password: String)
}