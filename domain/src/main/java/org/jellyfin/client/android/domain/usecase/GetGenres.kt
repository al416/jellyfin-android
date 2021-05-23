package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetGenres @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                     private val viewsRepository: ViewsRepository
) : BaseUseCase<List<Genre>, GetGenres.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<Genre>>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getGenres(params.libraryId, params.itemType)
    }

    data class RequestParams(val libraryId: UUID, val itemType: ItemType)
}