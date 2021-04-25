package org.jellyfin.client.android.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.display_model.Login
import org.jellyfin.client.android.network.Resource

interface LoginRepository {

    suspend fun doUserLogin(baseUrl: String, username: String, password: String): Flow<Resource<Login>>

}