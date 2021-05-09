package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.EmptyModel
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Named


class AddServer @Inject constructor(
    @Named("disk") diskDispatcher: CoroutineDispatcher,
    private val loginRepository: LoginRepository
) : BaseUseCase<EmptyModel, AddServer.RequestParams>(diskDispatcher) {

    @ExperimentalCoroutinesApi
    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<EmptyModel>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        val sanitizedName = sanitizeName(params.serverName)
        val sanitizedUrl = sanitizeUrl(params.serverUrl)
        val validateName = validateServerName(params.serverList, sanitizedName)
        val validateUrl = validateServerUrl(params.serverList, sanitizedUrl)
        return when {
            validateName != null -> {
                flow { emit(Resource.error<EmptyModel>(listOf(validateName))) }
            }
            validateUrl != null -> {
                flow { emit(Resource.error<EmptyModel>(listOf(validateUrl))) }
            }
            else -> {
                loginRepository.addServer(
                    Server(
                        id = 0,
                        name = sanitizedName,
                        url = sanitizedUrl,
                        displayOrder = params.serverList.size
                    )
                )
                flow { emit(Resource.success(EmptyModel(0))) }
            }
        }
    }

    private fun sanitizeName(name: String): String {
        return name.trim()
    }

    private fun sanitizeUrl(url: String): String {
        val result= if (url.endsWith("/")) return url.dropLast(1) else url
        return result.trim()
    }

    private fun validateServerName(serverList: List<Server>, serverName: String): Error? {
        val isBlank = serverName.isBlank()
        val serverExists = serverList.filter { it.name == serverName }
        return when {
            isBlank -> {
                Error(0, 0, "Please enter a valid server name", null)
            }
            serverExists.isNotEmpty() -> Error(0, 0, "A server with this name has already been added! Please enter an unique server name", null)
            else -> {
                null
            }
        }
    }

    private fun validateServerUrl(serverList: List<Server>, serverUrl: String): Error? {
        val isBlank = serverUrl.isBlank()
        val serverExists = serverList.filter { it.url == serverUrl }
        return when {
            isBlank -> {
                Error(0, 0, "Please enter a valid server URL", null)
            }
            serverExists.isNotEmpty() -> Error(0, 0, "A server with this URL has already been added! Please enter an unique server name", null)
            else -> {
                null
            }
        }
    }

    data class RequestParams(val serverList: List<Server>, val serverName: String, val serverUrl: String)
}