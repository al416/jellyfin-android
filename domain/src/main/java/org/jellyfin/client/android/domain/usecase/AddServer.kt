package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.EmptyModel
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class AddServer @Inject constructor(
    @Named("disk") diskDispatcher: CoroutineDispatcher,
    private val loginRepository: LoginRepository,
    private val getServerList: GetServerList
) : BaseUseCase<EmptyModel, AddServer.RequestParams>(diskDispatcher) {

    @ExperimentalCoroutinesApi
    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<EmptyModel>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        getServerList.invoke().flatMapLatest {
            when (it.status) {
                Status.ERROR -> flow { emit(Resource.error<EmptyModel>(it.messages)) }
                Status.LOADING -> flow { emit(Resource.loading<EmptyModel>()) }
                Status.SUCCESS -> {
                    val serverList = it.data ?: emptyList()
                    if (isValidServerName(serverList, params.serverName) && isValidServerUrl(serverList,params.serverUrl)) {
                        loginRepository.addServer(
                            Server(
                                id = 0,
                                name = params.serverName,
                                url = params.serverUrl,
                                displayOrder = serverList.size
                            )
                        )
                        flow { emit(Resource.success(EmptyModel(0))) }
                    } else {
                        flow { emit(Resource.error<EmptyModel>(listOf(Error(0, 0, "", null)))) }
                    }
                }
            }
        }

        return flow { emit(Resource.success(EmptyModel(0))) }
    }

    private fun isValidServerName(serverList: List<Server>, serverName: String): Boolean {
        return true
    }

    private fun isValidServerUrl(serverList: List<Server>, serverUrl: String): Boolean {
        return true
    }

    data class RequestParams(val serverName: String, val serverUrl: String)
}