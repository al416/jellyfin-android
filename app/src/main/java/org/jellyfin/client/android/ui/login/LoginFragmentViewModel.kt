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
import org.jellyfin.client.android.domain.usecase.DoUserLogin
import javax.inject.Inject
import javax.inject.Named

class LoginFragmentViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val doUserLogin: DoUserLogin
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
}