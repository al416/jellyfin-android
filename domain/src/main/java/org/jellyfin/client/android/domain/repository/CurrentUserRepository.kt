package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Session
import java.util.*

// This repository holds information about the current logged in user
// TODO: Add a scope to this repository

interface CurrentUserRepository {

    suspend fun getCurrentUserId(): UUID?

    suspend fun getBaseUrl(): String?

    suspend fun getCurrentSession(): Flow<Resource<Session>>

}