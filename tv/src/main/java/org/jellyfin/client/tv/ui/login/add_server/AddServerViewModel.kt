package org.jellyfin.client.tv.ui.login.add_server

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.jellyfin.client.android.domain.usecase.UpdateServers
import javax.inject.Inject
import javax.inject.Named

class AddServerViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val updateServers: UpdateServers

) : ViewModel() {


    fun addServer(serverId: Int, serverName: String, serverUrl: String) {

    }
}