package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.VideoPlayType
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.jellyfin.client.android.domain.repository.MediaRepository
import org.jellyfin.sdk.api.operations.MediaInfoApi
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.api.operations.VideosApi
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MediaRepositoryImpl @Inject constructor(@Named("network") private val networkDispatcher: CoroutineDispatcher,
                                              private val mediaInfoApi: MediaInfoApi,
                                              private val userApi: UserApi,
                                              private val deviceProfile: DeviceProfile,
                                              private val videosApi: VideosApi
) : MediaRepository {

    override suspend fun getTranscodingUrl(mediaId: UUID, userId: UUID): Flow<Resource<VideoPlaybackInformation>> {
        return flow<Resource<VideoPlaybackInformation>> {
            emit(Resource.loading())
            try {
                val uuid = userApi.getCurrentUser().content.id  // TODO: Remove this line because this will be passed by the use case
                // TODO: Set this constants correctly
                val playbackInfoDto = PlaybackInfoDto(userId = uuid, startTimeTicks = 47451096240, maxStreamingBitrate = 243478261,
                    autoOpenLiveStream = true, enableTranscoding = true, deviceProfile = deviceProfile)
                val result by mediaInfoApi.getPostedPlaybackInfo(itemId = mediaId, playbackInfoDto)
                val transcodingUrl = result.mediaSources?.first()?.transcodingUrl

                val baseUrl = ""    // TODO: Create the transcoding string correctly using the base URL
                emit(Resource.success(VideoPlaybackInformation("$baseUrl$transcodingUrl", VideoPlayType.TRANSCODING)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                emit(Resource.error(listOf(Error(httpErrorResponseCode = null, code = 1, message = e.message, exception = null))))
            }

        }.flowOn(networkDispatcher)
    }

    override suspend fun getDirectPlayUrl(
        mediaId: UUID
    ): Flow<Resource<VideoPlaybackInformation>> {
        return flow<Resource<VideoPlaybackInformation>> {
            emit(Resource.loading())
            try {
                val directPlayUrl = videosApi.getVideoStreamUrl(itemId = mediaId, container = "mkv", static = true)
                emit(Resource.success(VideoPlaybackInformation(directPlayUrl, VideoPlayType.DIRECT_PLAY)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                emit(Resource.error(listOf(Error(httpErrorResponseCode = null, code = 1, message = e.message, exception = null))))
            }

        }.flowOn(networkDispatcher)
    }

}