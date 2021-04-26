package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeContents
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import java.util.*

interface ViewsRepository {

    suspend fun getMyMediaSection(userId: UUID): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getContinueWatchingSection(userId: UUID, mediaTypes: List<String>?): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getNextUpSection(userId: UUID): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getLatestSection(userId: UUID, libraries: List<LibraryDto>): Flow<Resource<HomeContents>>

    suspend fun getHomeSections(userId: UUID): Flow<Resource<List<HomeSectionType>>>
}