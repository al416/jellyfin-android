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
import org.jellyfin.client.android.domain.models.Session
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.usecase.DoUserLogin
import org.jellyfin.client.android.domain.usecase.DoUserLogout
import org.jellyfin.client.android.domain.usecase.GetCurrentSession
import org.jellyfin.client.android.domain.usecase.GetServerList
import org.jellyfin.sdk.api.client.KtorClient
import javax.inject.Inject
import javax.inject.Named

class LoginViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val doUserLogin: DoUserLogin,
    private val doUserLogout: DoUserLogout,
    private val getServerList: GetServerList,
    private val getCurrentSession: GetCurrentSession,
    private val api: KtorClient
) : ViewModel() {

    private val loginState = MutableLiveData<Resource<Login>?>(null)

    fun getLoginState(): LiveData<Resource<Login>?> {
        return loginState
    }
    
    fun resetLoginState() {
        loginState.postValue(null)
    }

    fun doUserLogin(server: Server, username: String, password: String) {
        viewModelScope.launch(computationDispatcher) {
            doUserLogin.invoke(DoUserLogin.RequestParams(server, username, password))
                .collectLatest {
                    loginState.postValue(it)
                }
        }
    }

    fun doUserLogout() {
        viewModelScope.launch(computationDispatcher) {
            doUserLogout.invoke()
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

    private val session: MutableLiveData<Resource<Session>> by lazy {
        val data = MutableLiveData<Resource<Session>>()
        loadSession(data)
        data
    }

    fun getCurrentSession(): LiveData<Resource<Session>> {
        return session
    }

    private fun loadSession(data: MutableLiveData<Resource<Session>>) {
        viewModelScope.launch(computationDispatcher) {
            getCurrentSession.invoke().collectLatest {
                it.data?.let {session ->
                    api.baseUrl = session.serverUrl
                    api.accessToken = session.apiKey
                }
                data.postValue(it)
            }
        }
    }
}