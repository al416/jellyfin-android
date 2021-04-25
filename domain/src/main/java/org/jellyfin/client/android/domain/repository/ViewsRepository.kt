package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import java.util.*

interface ViewsRepository {

    suspend fun getMyMediaSection(userId: UUID): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getContinueWatchingSection(userId: UUID, mediaTypes: List<String>?): Flow<Resource<List<HomeSectionCard>>>

}