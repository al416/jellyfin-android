package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.jellyfin.client.android.data.database.SessionDao
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Session
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class CurrentUserRepositoryImpl @Inject constructor(@Named("disk") private val diskDispatcher: CoroutineDispatcher,
                                                    private val sessionDao: SessionDao
) : CurrentUserRepository {

    private var currentSession: Session? = null

    override suspend fun getCurrentUserId(): UUID? {
        if (currentSession != null) {
            return UUID.fromString(currentSession?.userUUID)
        }
        return null
    }

    override suspend fun getBaseUrl(): String? {
        if (currentSession != null) {
            return currentSession?.serverUrl
        }
        return null
    }

    override suspend fun getCurrentSession(): Flow<Resource<Session>> {
        return sessionDao.getCurrentSession().map {
            currentSession = it
            Resource.success(it)
        }.flowOn(diskDispatcher)
    }

}