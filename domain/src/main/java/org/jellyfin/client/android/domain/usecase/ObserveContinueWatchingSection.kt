package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ObserveContinueWatchingSection @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                                         private val viewsRepository: ViewsRepository
) : BaseUseCase<List<HomeSectionCard>, ObserveContinueWatchingSection.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<HomeSectionCard>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return viewsRepository.getContinueWatchingSection(params.userId, params.mediaTypes)
    }

    data class RequestParams(val userId: UUID, val mediaTypes: List<String>?)
}