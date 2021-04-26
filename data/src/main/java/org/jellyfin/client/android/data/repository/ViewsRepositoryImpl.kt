package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeContents
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.api.operations.UserLibraryApi
import org.jellyfin.sdk.api.operations.UserViewsApi
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class ViewsRepositoryImpl @Inject constructor(@Named("computation") private val computationDispatcher: CoroutineDispatcher,
                                              private val userViewsApi: UserViewsApi,
                                              private val itemsApi: ItemsApi,
                                              private val tvShowsApi: TvShowsApi,
                                              private val userLibraryApi: UserLibraryApi
) : ViewsRepository {
    override suspend fun getMyMediaSection(userId: UUID): Flow<Resource<List<HomeSectionCard>>> {
        return flow<Resource<List<HomeSectionCard>>> {
            emit(Resource.loading())
            try {
                val response = mutableListOf<HomeSectionCard>()
                val result by userViewsApi.getUserViews(userId)
                result.items?.forEachIndexed {index, item ->
                    response.add(HomeSectionCard(id = index, backgroundImage = 0, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.DETAILS))
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(null, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }

    override suspend fun getContinueWatchingSection(userId: UUID, mediaTypes: List<String>?): Flow<Resource<List<HomeSectionCard>>> {
        return flow<Resource<List<HomeSectionCard>>> {
            emit(Resource.loading())
            try {
                val response = mutableListOf<HomeSectionCard>()
                val result by itemsApi.getResumeItems(userId = userId,
                    limit = 12,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.THUMB),
                    mediaTypes = mediaTypes)
                result.items?.forEachIndexed {index, item ->
                    response.add(HomeSectionCard(id = index, backgroundImage = 0, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.DETAILS))
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }

    override suspend fun getNextUpSection(userId: UUID): Flow<Resource<List<HomeSectionCard>>> {
        return flow<Resource<List<HomeSectionCard>>> {
            emit(Resource.loading())
            try {
                val response = mutableListOf<HomeSectionCard>()
                val result by tvShowsApi.getNextUp(userId = userId,
                    limit = 24,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB),
                    disableFirstEpisode = true)
                result.items?.forEachIndexed {index, item ->
                    response.add(HomeSectionCard(id = index, backgroundImage = 0, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.DETAILS))
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }

    override suspend fun getLatestSection(userId: UUID, libraries: List<LibraryDto>): Flow<Resource<HomeContents>> {
        return flow<Resource<HomeContents>> {
            emit(Resource.loading())
            try {
                val rows = mutableListOf<HomeSectionRow>()
                val cards = mutableListOf<HomeSectionCard>()
                var index = 0
                libraries.forEach { library ->
                    val result by userLibraryApi.getLatestMedia(userId = userId,
                        parentId = library.id,
                        limit = 16,
                        fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO, ItemFields.PATH),
                        imageTypeLimit = 1,
                        enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.THUMB))
                    if (result.isNotEmpty()) {
                        // TODO: Use a string repository to get the "Latest x" string
                        rows.add(HomeSectionRow(id = index, title = "Latest " + library.title))
                        result.forEachIndexed { resultIndex, item ->
                            cards.add(HomeSectionCard(id = resultIndex, backgroundImage = 0, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.DETAILS, rowId = index))
                        }
                        index++
                    }
                }
                emit(Resource.success(HomeContents(rows, cards)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }

    override suspend fun getHomeSections(userId: UUID): Flow<Resource<List<HomeSectionType>>> {
        return flow<Resource<List<HomeSectionType>>> {
            emit(Resource.loading())
            try {
                // TODO: Figure out the API call the returns the actual list of home sections and add them to the response in the correct order
                val response = mutableListOf<HomeSectionType>()
                response.add(HomeSectionType.MY_MEDIA)
                response.add(HomeSectionType.CONTINUE_WATCHING)
                response.add(HomeSectionType.NEXT_UP)
                response.add(HomeSectionType.LATEST_MEDIA)
                emit(Resource.success(response))
            } catch (e: Exception) {
                emit(Resource.error(listOf(Error(null, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }
}