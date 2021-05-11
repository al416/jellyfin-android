package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class DoUserLogout @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                      private val loginRepository: LoginRepository
) : BaseUseCase<Login, Any?>(dispatcher) {

    override suspend fun invokeInternal(params: Any?): Flow<Resource<Login>> {
        loginRepository.doUserLogout()
        return flow { emit(Resource.success(Login(null))) }
    }
}