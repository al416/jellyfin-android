package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import java.util.*

interface ViewsRepository {

    suspend fun getMyMediaSection(retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>>

    suspend fun getContinueWatchingSection(mediaTypes: List<String>?, retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>>

    suspend fun getNextUpSection(retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>>

    suspend fun getLatestSection(libraries: List<LibraryDto>, retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>>

    suspend fun getHomeSections(): Flow<Resource<List<HomeSectionType>>>

    suspend fun getRecentItems(): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getMovieDetails(movieId: UUID): Flow<Resource<MovieDetails>>

    fun clearCache()
}