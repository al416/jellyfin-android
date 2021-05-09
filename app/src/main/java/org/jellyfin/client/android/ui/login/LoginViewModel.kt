package org.jellyfin.client.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.EmptyModel
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.usecase.AddServer
import org.jellyfin.client.android.domain.usecase.DoUserLogin
import org.jellyfin.client.android.domain.usecase.GetServerList
import javax.inject.Inject
import javax.inject.Named

class LoginViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val doUserLogin: DoUserLogin,
    private val getServerList: GetServerList,
    private val addServer: AddServer

) : ViewModel() {

    private val loginState = MutableLiveData<Resource<Login>?>(null)

    fun getLoginState(): LiveData<Resource<Login>?> {
        return loginState
    }

    fun doUserLogin(baseUrl: String, username: String, password: String) {
        viewModelScope.launch(computationDispatcher) {
            doUserLogin.invoke(DoUserLogin.RequestParams(baseUrl, username, password))
                .collectLatest {
                    loginState.postValue(it)
                }
        }
    }

    private val servers: MutableLiveData<Resource<List<Server>>> by lazy {
        val data = MutableLiveData<Resource<List<Server>>>()
        loadServers(data)
        data
    }

    fun getServers(): LiveData<Resource<List<Server>>> {
        return servers
    }

    private fun loadServers(data: MutableLiveData<Resource<List<Server>>>) {
        viewModelScope.launch(computationDispatcher) {
            getServerList.invoke().collectLatest {
                data.postValue(it)
            }
        }
    }

    private val addServerStatus = MutableLiveData<Resource<EmptyModel>?>(null)

    fun getAddServerStatus(): LiveData<Resource<EmptyModel>?> {
        return addServerStatus
    }

    fun addServer(serverName: String, serverUrl: String) {
        viewModelScope.launch(computationDispatcher) {
            addServer.invoke(AddServer.RequestParams(serverName, serverUrl)).collectLatest {
                addServerStatus.postValue(it)
            }
        }
    }

}