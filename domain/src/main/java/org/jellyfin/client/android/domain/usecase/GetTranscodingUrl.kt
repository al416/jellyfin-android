package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.jellyfin.client.android.domain.repository.MediaRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetTranscodingUrl @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                               private val mediaRepository: MediaRepository
) : BaseUseCase<VideoPlaybackInformation, GetTranscodingUrl.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<VideoPlaybackInformation>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return mediaRepository.getTranscodingUrl(params.mediaId)
    }

    data class RequestParams(val mediaId: UUID)
}