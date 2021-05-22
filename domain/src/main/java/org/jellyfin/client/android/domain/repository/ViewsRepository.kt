package org.jellyfin.client.android.domain.repository

import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.models.display_model.Season
import org.jellyfin.client.android.domain.models.display_model.SeriesDetails
import java.util.*

interface ViewsRepository {

    suspend fun getMyMediaSection(retrieveFromCache: Boolean): Flow<Resource<List<Library>>>

    suspend fun getContinueWatchingSection(mediaTypes: List<String>?, retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>>

    suspend fun getNextUpSection(retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>>

    suspend fun getLatestSection(libraries: List<Library>, retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>>

    suspend fun getHomeSections(): Flow<Resource<List<HomeSectionType>>>

    suspend fun getRecentItems(): Flow<Resource<List<HomeSectionCard>>>

    suspend fun getMovieDetails(movieId: UUID): Flow<Resource<MovieDetails>>

    suspend fun getSeriesDetails(seriesId: UUID): Flow<Resource<SeriesDetails>>

    suspend fun getSeasons(seriesId: UUID): Flow<Resource<List<Season>>>

    suspend fun getLibraryItems(pageNumber: Int, pageSize: Int, library: Library): List<HomeSectionCard>

    fun clearCache()
}