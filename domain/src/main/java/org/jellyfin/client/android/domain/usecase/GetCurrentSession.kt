package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Session
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import javax.inject.Inject
import javax.inject.Named


class GetCurrentSession @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                            private val currentUserRepository: CurrentUserRepository
) : BaseUseCase<Session, Any?>(dispatcher) {

    override suspend fun invokeInternal(params: Any?): Flow<Resource<Session>> {
        return currentUserRepository.getCurrentSession()
    }
}