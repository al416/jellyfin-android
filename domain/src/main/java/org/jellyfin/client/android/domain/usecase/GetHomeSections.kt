package org.jellyfin.client.android.domain.usecase


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetHomeSections @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                               private val viewsRepository: ViewsRepository
) : BaseUseCase<List<HomeSectionType>, GetHomeSections.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<HomeSectionType>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return viewsRepository.getHomeSections(params.userId)
    }

    data class RequestParams(val userId: UUID)
}