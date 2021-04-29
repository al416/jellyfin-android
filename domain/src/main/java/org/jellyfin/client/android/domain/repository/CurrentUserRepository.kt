package org.jellyfin.client.android.domain.repository

import java.util.*

// This repository holds information about the current logged in user
// TODO: Add a scope to this repository

interface CurrentUserRepository {

    suspend fun getCurrentUserId(): UUID

    suspend fun setCurrentUserId(userId: UUID)

    suspend fun getBaseUrl(): String

    suspend fun setBaseUrl(url: String)

}