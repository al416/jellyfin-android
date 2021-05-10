package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.EmptyModel
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class UpdateServers @Inject constructor(
    @Named("disk") diskDispatcher: CoroutineDispatcher,
    private val loginRepository: LoginRepository
) : BaseUseCase<EmptyModel, UpdateServers.RequestParams>(diskDispatcher) {

    @ExperimentalCoroutinesApi
    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<EmptyModel>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        loginRepository.deleteAllServers()
        loginRepository.addServers(params.servers)
        return flow { emit(Resource.success(EmptyModel(0))) }
    }

    data class RequestParams(val servers: List<Server>)
}