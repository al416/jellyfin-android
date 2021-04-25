package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Login
import org.jellyfin.client.android.domain.models.Resource

interface LoginRepository {

    suspend fun doUserLogin(baseUrl: String, username: String, password: String): Flow<Resource<Login>>

}