package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class GetServerList @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                      private val loginRepository: LoginRepository
) : BaseUseCase<List<Server>, Any?>(dispatcher) {

    override suspend fun invokeInternal(params: Any?): Flow<Resource<List<Server>>> {
        return loginRepository.getServerList()
    }
}