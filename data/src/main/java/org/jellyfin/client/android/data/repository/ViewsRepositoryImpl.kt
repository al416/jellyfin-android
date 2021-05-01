package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.constants.LibraryType
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.api.operations.UserLibraryApi
import org.jellyfin.sdk.api.operations.UserViewsApi
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class ViewsRepositoryImpl @Inject constructor(@Named("network") private val networkDispatcher: CoroutineDispatcher,
                                              private val userViewsApi: UserViewsApi,
                                              private val itemsApi: ItemsApi,
                                              private val tvShowsApi: TvShowsApi,
                                              private val userLibraryApi: UserLibraryApi,
                                              private val imageApi: ImageApi,
                                              private val currentUserRepository: CurrentUserRepository
) : ViewsRepository {
    override suspend fun getMyMediaSection(): Flow<Resource<HomeSectionRow>> {
        val userId = currentUserRepository.getCurrentUserId()
        return flow<Resource<HomeSectionRow>> {
            emit(Resource.loading())
            try {
                val cards = mutableListOf<HomeSectionCard>()
                val result by userViewsApi.getUserViews(userId)
                result.items?.forEachIndexed {index, item ->
                    val imageUrl = imageApi.getItemImageUrl(itemId = item.id, imageType = ImageType.PRIMARY)
                    cards.add(HomeSectionCard(id = index, imageUrl = imageUrl, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.BACKDROP))
                }
                // TODO: Use a repo to get My Media string
                emit(Resource.success(HomeSectionRow(id = HomeSectionType.MY_MEDIA.ordinal, title = "My Media", cards = cards)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(null, 1, "Error", null))))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getContinueWatchingSection(mediaTypes: List<String>?): Flow<Resource<HomeSectionRow>> {
        val userId = currentUserRepository.getCurrentUserId()
        return flow<Resource<HomeSectionRow>> {
            emit(Resource.loading())
            try {
                val cards = mutableListOf<HomeSectionCard>()
                val result by itemsApi.getResumeItems(userId = userId,
                    limit = 12,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.THUMB),
                    mediaTypes = mediaTypes)
                result.items?.forEachIndexed {index, item ->
                    var itemId = item.id
                    if (item.backdropImageTags.isNullOrEmpty() && !item.parentBackdropItemId.isNullOrBlank() && item.seriesId != null) {
                        itemId = item.seriesId!!
                    }
                    val imageUrl = imageApi.getItemImageUrl(itemId = itemId, imageType = ImageType.BACKDROP)

                    cards.add(HomeSectionCard(id = index, imageUrl = imageUrl, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.BACKDROP))
                }
                // TODO: Use a repo to get Continue Watching string
                emit(Resource.success(HomeSectionRow(id = HomeSectionType.CONTINUE_WATCHING.ordinal, title = "Continue Watching", cards = cards)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getNextUpSection(): Flow<Resource<HomeSectionRow>> {
        val userId = currentUserRepository.getCurrentUserId()
        return flow<Resource<HomeSectionRow>> {
            emit(Resource.loading())
            try {
                val cards = mutableListOf<HomeSectionCard>()
                val result by tvShowsApi.getNextUp(userId = userId,
                    limit = 24,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB),
                    disableFirstEpisode = true)
                result.items?.forEachIndexed {index, item ->
                    val imageUrl = imageApi.getItemImageUrl(itemId = item.id, imageType = ImageType.BACKDROP)
                    cards.add(HomeSectionCard(id = index, imageUrl = imageUrl, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.BACKDROP))
                }
                // TODO: Use a repo to get Next Up string
                emit(Resource.success(HomeSectionRow(id = HomeSectionType.NEXT_UP.ordinal, title = "Next Up", cards = cards)))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getLatestSection(libraries: List<LibraryDto>): Flow<Resource<List<HomeSectionRow>>> {
        val userId = currentUserRepository.getCurrentUserId()
        return flow<Resource<List<HomeSectionRow>>> {
            emit(Resource.loading())
            try {
                val rows = mutableListOf<HomeSectionRow>()
                libraries.forEachIndexed { libraryIndex, library ->
                    val result by userLibraryApi.getLatestMedia(userId = userId,
                        parentId = library.id,
                        limit = 16,
                        fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.BASIC_SYNC_INFO, ItemFields.PATH),
                        imageTypeLimit = 1,
                        enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.THUMB))
                    if (result.isNotEmpty()) {
                        val cards = mutableListOf<HomeSectionCard>()
                        // TODO: Use a string repository to get the "Latest x" string
                        result.forEachIndexed { resultIndex, item ->
                            val imageUrl = imageApi.getItemImageUrl(itemId = item.id, imageType = ImageType.PRIMARY, fillWidth = 223, fillHeight = 335)
                            cards.add(HomeSectionCard(id = resultIndex, imageUrl = imageUrl, title = item.name, subtitle = null, uuid = item.id, homeCardType = HomeCardType.POSTER))
                        }
                        rows.add(HomeSectionRow(id = HomeSectionType.LATEST_MEDIA.ordinal + libraryIndex, title = "Latest " + library.title, cards = cards))
                    }
                }
                // TODO: Use a repo to get My Media string
                emit(Resource.success(rows))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getHomeSections(): Flow<Resource<List<HomeSectionType>>> {
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
        }.flowOn(networkDispatcher)
    }

    override suspend fun getRecentItems(): Flow<Resource<List<HomeSectionCard>>> {
        val userId = currentUserRepository.getCurrentUserId()
        return flow<Resource<List<HomeSectionCard>>> {
            emit(Resource.loading())
            try {
                val result by userLibraryApi.getLatestMedia(userId = userId,
                    limit = 10,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.OVERVIEW),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.LOGO, ImageType.BACKDROP))
                // TODO: This filters out any item that was added recently if it does not have a backdrop image.
                //  Is this desirable or should a placeholder be loaded if there is no backdrop image?
                val filteredResult = result.filter { it.backdropImageTags?.isNotEmpty() ?: false }
                val response = mutableListOf<HomeSectionCard>()
                filteredResult.forEachIndexed { index, item ->
                    val imageUrl = imageApi.getItemImageUrl(itemId = item.id, imageType = ImageType.BACKDROP)
                    response.add(HomeSectionCard(id = index, imageUrl = imageUrl, title = item.name, subtitle = null, homeCardType = HomeCardType.BACKDROP, uuid = item.id))
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                emit(Resource.error(listOf(Error(null, 1, "Error", null))))
            }
        }.flowOn(networkDispatcher)
    }
}