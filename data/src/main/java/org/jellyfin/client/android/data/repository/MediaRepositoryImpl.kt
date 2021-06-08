package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.VideoPlayType
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.MediaRepository
import org.jellyfin.sdk.api.operations.MediaInfoApi
import org.jellyfin.sdk.api.operations.VideosApi
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MediaRepositoryImpl @Inject constructor(@Named("network") private val networkDispatcher: CoroutineDispatcher,
                                              private val mediaInfoApi: MediaInfoApi,
                                              private val deviceProfile: DeviceProfile,
                                              private val videosApi: VideosApi,
                                              private val currentUserRepository: CurrentUserRepository
) : MediaRepository {

    override suspend fun getTranscodingUrl(mediaId: UUID): Flow<Resource<VideoPlaybackInformation>> {
        val userId = currentUserRepository.getCurrentUserId()
        val baseUrl = currentUserRepository.getBaseUrl()
        if (userId == null || baseUrl == null) {
            throw IllegalArgumentException("UserId or Server URL cannot be null")
        }
        return flow<Resource<VideoPlaybackInformation>> {
            emit(Resource.loading())
            try {
                // TODO: Set this constants correctly
                val playbackInfoDto = PlaybackInfoDto(userId = userId, startTimeTicks = 47451096240, maxStreamingBitrate = 243478261,
                    autoOpenLiveStream = true, enableTranscoding = true, deviceProfile = deviceProfile)
                val result by mediaInfoApi.getPostedPlaybackInfo(itemId = mediaId, playbackInfoDto)

                val mediaSourceInfo = result.mediaSources?.first()
                // TODO: Clean up all this logic to figure out which URL to use
                if (mediaSourceInfo != null) {
                    if (mediaSourceInfo.path != null && mediaSourceInfo.path!!.startsWith("http")) {
                        emit(Resource.success(VideoPlaybackInformation(mediaSourceInfo.path, VideoPlayType.DIRECT_PLAY)))
                    } else if (mediaSourceInfo.supportsDirectPlay && mediaSourceInfo.transcodingUrl == null) {
                        emit(Resource.error(listOf(Error(0, 0, "Could not get transcoding URL", null))))
                    } else {
                        val transcodingUrl = mediaSourceInfo.transcodingUrl
                        emit(Resource.success(VideoPlaybackInformation("$baseUrl$transcodingUrl", VideoPlayType.TRANSCODING)))
                    }
                } else {
                    emit(Resource.error(listOf(Error(0, 0, "Could not get transcoding URL", null))))
                }
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
