package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class CurrentUserRepositoryImpl @Inject constructor(@Named("computation") private val computationDispatcher: CoroutineDispatcher
) : CurrentUserRepository {

    private lateinit var currentUserId: UUID
    private lateinit var baseUrl: String

    override suspend fun getCurrentUserId(): UUID {
        return currentUserId
    }

    override suspend fun setCurrentUserId(userId: UUID) {
        currentUserId = userId
    }

    override suspend fun getBaseUrl(): String {
        return baseUrl
    }

    override suspend fun setBaseUrl(url: String) {
        baseUrl = url
    }

}