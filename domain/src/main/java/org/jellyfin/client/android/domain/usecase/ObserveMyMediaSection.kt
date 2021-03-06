package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.repository.ViewsRepository
import javax.inject.Inject
import javax.inject.Named

class ObserveMyMediaSection @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                                private val viewsRepository: ViewsRepository
) : BaseUseCase<List<Library>, ObserveMyMediaSection.RequestParam?>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParam?): Flow<Resource<List<Library>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }
        return viewsRepository.getMyMediaSection(params.retrieveFromCache)
    }

    data class RequestParam(val retrieveFromCache: Boolean)
}