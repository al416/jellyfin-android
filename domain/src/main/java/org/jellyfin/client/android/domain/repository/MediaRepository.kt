package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import java.util.*

interface MediaRepository {

    suspend fun getTranscodingUrl(mediaId: UUID, userId: UUID): Flow<Resource<VideoPlaybackInformation>>

    suspend fun getDirectPlayUrl(mediaId: UUID): Flow<Resource<VideoPlaybackInformation>>

}