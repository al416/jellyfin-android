package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import java.util.*

interface ViewsRepository {

    suspend fun getMyMediaSection(userId: UUID): Flow<Resource<HomeSectionRow>>

    suspend fun getContinueWatchingSection(userId: UUID, mediaTypes: List<String>?): Flow<Resource<HomeSectionRow>>

    suspend fun getNextUpSection(userId: UUID): Flow<Resource<HomeSectionRow>>

    suspend fun getLatestSection(userId: UUID, libraries: List<LibraryDto>): Flow<Resource<List<HomeSectionRow>>>

    suspend fun getHomeSections(userId: UUID): Flow<Resource<List<HomeSectionType>>>
}