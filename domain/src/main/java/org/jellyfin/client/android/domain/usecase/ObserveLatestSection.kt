package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ObserveLatestSection @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                               private val viewsRepository: ViewsRepository
) : BaseUseCase<List<HomeSectionRow>, ObserveLatestSection.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<HomeSectionRow>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return viewsRepository.getLatestSection(params.libraries)
    }

    data class RequestParams(val libraries: List<LibraryDto>)
}