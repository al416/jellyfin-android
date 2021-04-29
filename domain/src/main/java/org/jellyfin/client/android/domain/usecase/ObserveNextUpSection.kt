package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ObserveNextUpSection @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                               private val viewsRepository: ViewsRepository
) : BaseUseCase<HomeSectionRow, Any?>(dispatcher) {

    override suspend fun invokeInternal(params: Any?): Flow<Resource<HomeSectionRow>> {
        return viewsRepository.getNextUpSection()
    }
}