package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Season
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetSeasons @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                           private val viewsRepository: ViewsRepository
) : BaseUseCase<List<Season>, GetSeasons.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<Season>>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getSeasons(params.seriesId)
    }

    data class RequestParams(val seriesId: UUID)
}