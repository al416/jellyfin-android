package org.jellyfin.client.android.domain.usecase


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetVideoPlaybackInformation @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                               private val getTranscodingUrl: GetTranscodingUrl,
                                               private val getDirectPlayUrl: GetDirectPlayUrl
) : BaseUseCase<VideoPlaybackInformation, GetVideoPlaybackInformation.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<VideoPlaybackInformation>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return getTranscodingUrl.invoke(GetTranscodingUrl.RequestParams(params.mediaId, params.userId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> flow { emit(Resource.error<VideoPlaybackInformation>(it.messages)) }
                Status.LOADING -> flow {emit(Resource.loading<VideoPlaybackInformation>())}
                Status.SUCCESS -> {
                    // The server did not provide a valid transcoding URL so try to get a direct play URL
                    if (it.data?.url.isNullOrBlank()) {
                        getDirectPlayUrl(params.mediaId)
                    } else {
                        flow { emit(Resource.success(it.data)) }
                    }
                }
            }
        }
    }

    private suspend fun getDirectPlayUrl(mediaId: UUID): Flow<Resource<VideoPlaybackInformation>> {
        return getDirectPlayUrl.invoke(GetDirectPlayUrl.RequestParams(mediaId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> flow { emit(Resource.error<VideoPlaybackInformation>(it.messages)) }
                Status.LOADING -> flow {emit(Resource.loading<VideoPlaybackInformation>())}
                Status.SUCCESS -> {
                    // The server did not provide a valid direct play URL so throw an error
                    if (it.data?.url.isNullOrBlank()) {
                        // TODO: Use correct error
                        flow { emit(Resource.error<VideoPlaybackInformation>(listOf(Error(0, 0, "Could not retrieve streaming URL", null)))) }
                    } else {
                        flow { emit(Resource.success(it.data)) }
                    }
                }
            }
        }
    }

    data class RequestParams(val mediaId: UUID, val userId: UUID)
}