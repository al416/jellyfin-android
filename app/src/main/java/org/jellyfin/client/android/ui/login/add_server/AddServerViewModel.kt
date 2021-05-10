package org.jellyfin.client.android.ui.login.add_server

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.EmptyModel
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.usecase.UpdateServers
import javax.inject.Inject
import javax.inject.Named

class AddServerViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val updateServers: UpdateServers

) : ViewModel() {

    fun initialize(servers: List<Server>) {
        this.servers.postValue(servers)
    }

    private val servers: MutableLiveData<List<Server>> = MutableLiveData()

    fun getServers(): LiveData<List<Server>> {
        return servers
    }

    private val addServerStatus = MutableLiveData<Resource<EmptyModel>?>(null)

    fun getAddServerStatus(): LiveData<Resource<EmptyModel>?> {
        return addServerStatus
    }

    private val updateServersStatus = MutableLiveData<Resource<EmptyModel>?>(null)

    fun getUpdateServersStatus(): LiveData<Resource<EmptyModel>?> {
        return updateServersStatus
    }

    fun updateServers(servers: List<Server>) {
        viewModelScope.launch(computationDispatcher) {
            updateServers.invoke(UpdateServers.RequestParams(servers)).collectLatest {
                updateServersStatus.postValue(it)
            }
        }
    }

    fun addServer(serverName: String, serverUrl: String) {
        val serverList = servers.value ?: emptyList()

        val sanitizedName = sanitizeName(serverName)
        val sanitizedUrl = sanitizeUrl(serverUrl)
        val validateName = validateServerName(serverList, sanitizedName)
        val validateUrl = validateServerUrl(serverList, sanitizedUrl)

        if (validateName != null) {
            addServerStatus.postValue(Resource.error(listOf(validateName)))
        } else if (validateUrl != null) {
            addServerStatus.postValue(Resource.error(listOf(validateUrl)))
        } else {
            val newServers = mutableListOf<Server>()
            newServers.addAll(serverList)
            val server = Server(
                id = 0,
                name = sanitizedName,
                url = sanitizedUrl,
                displayOrder = serverList.size
            )
            newServers.add(server)
            servers.postValue(newServers)
            addServerStatus.postValue(Resource.success(null))
        }
    }

    private fun sanitizeName(name: String): String {
        return name.trim()
    }

    private fun sanitizeUrl(url: String): String {
        val result = if (url.endsWith("/")) return url.dropLast(1) else url
        return result.trim()
    }

    private fun validateServerName(serverList: List<Server>, serverName: String): Error? {
        val isBlank = serverName.isBlank()
        val serverExists = serverList.filter { it.name == serverName }
        return when {
            isBlank -> {
                Error(0, 0, "Please enter a valid server name", null)
            }
            serverExists.isNotEmpty() -> Error(
                0,
                0,
                "A server with this name has already been added! Please enter an unique server name",
                null
            )
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
            serverExists.isNotEmpty() -> Error(
                0,
                0,
                "A server with this URL has already been added! Please enter an unique server name",
                null
            )
            else -> {
                null
            }
        }
    }

}