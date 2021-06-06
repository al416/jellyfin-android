package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.cached_model.CachedRecentItem
import org.jellyfin.client.android.domain.models.display_model.RecentItem
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemFilter
import javax.inject.Inject
import javax.inject.Named

class ObserveRecentItems @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                             private val viewsRepository: ViewsRepository,
                                             private val currentUserRepository: CurrentUserRepository,
                                             private val tvShowsApi: TvShowsApi,
                                             private val imageApi: ImageApi,
                                             private val itemsApi: ItemsApi
) : BaseUseCase<List<RecentItem>, Any?>(dispatcher) {

    override suspend fun invokeInternal(params: Any?): Flow<Resource<List<RecentItem>>> {
        return viewsRepository.getRecentItems().flatMapLatest { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    processResult(resource.data)
                }
                Status.ERROR -> {
                    flow { emit(Resource.error<List<RecentItem>>(resource.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<List<RecentItem>>()) }
                }
            }
        }
    }

    private fun processResult(items: List<CachedRecentItem>?): Flow<Resource<List<RecentItem>>> {
        return flow {
            if (items.isNullOrEmpty()) {
                emit (Resource.success<List<RecentItem>>(emptyList()))
            } else {
                val userId = currentUserRepository.getCurrentUserId() ?: throw IllegalArgumentException("UserId cannot be null")
                // TODO: This filters out any item that was added recently if it does not have a backdrop image.
                //  Is this desirable or should a placeholder be loaded if there is no backdrop image?
                val filteredResult = items.filter { it.containsBackdropImages }
                val response = mutableListOf<RecentItem>()
                filteredResult.forEachIndexed { index, item ->
                    // TODO: All of this logic needs to be done on demand (i.e. AFTER user clicks the Play button then figure out which item to play next. This will be moved to a use case soon
                    val itemId = when (item.type) {
                        ItemType.EPISODE.type -> item.uuid
                        ItemType.SERIES.type -> {
                            val nextUpResults by tvShowsApi.getNextUp(
                                userId = userId,
                                limit = 1,
                                fields = listOf(
                                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                                    ItemFields.BASIC_SYNC_INFO
                                ),
                                imageTypeLimit = 1,
                                parentId = item.uuid,
                                enableImageTypes = listOf(ImageType.BACKDROP),
                                disableFirstEpisode = false
                            )
                            if (nextUpResults.items?.isEmpty() == true) {
                                val seriesItems by itemsApi.getItems(
                                    userId = userId,
                                    recursive = true,
                                    parentId = item.uuid,
                                    limit = 1,
                                    filters = listOf(ItemFilter.IS_NOT_FOLDER),
                                    mediaTypes = listOf("Video"), sortBy = listOf("SortName")
                                )
                                seriesItems.items?.first()?.id ?: item.uuid
                            } else {
                                nextUpResults.items?.first()?.id ?: item.uuid
                            }
                        }
                        else -> item.uuid
                    }
                    val title = when (item.type) {
                        ItemType.EPISODE.type -> item.seriesName
                        else -> item.name
                    }
                    val subtitle = when (item.type) {
                        ItemType.EPISODE.type -> item.name
                        else -> null
                    }
                    val imageItemId = if (item.type == ItemType.EPISODE.type) item.seriesId ?: item.uuid else item.uuid
                    val imageUrl = imageApi.getItemImageUrl(
                        itemId = imageItemId,
                        imageType = ImageType.BACKDROP
                    )
                    val itemType = when (item.type) {
                        ItemType.MOVIE.type -> ItemType.MOVIE
                        ItemType.SERIES.type -> ItemType.SERIES
                        ItemType.EPISODE.type -> ItemType.EPISODE
                        else -> ItemType.MOVIE
                    }
                    val seriesId = when (item.type) {
                        ItemType.EPISODE.type -> item.seriesId
                        ItemType.SERIES.type -> item.uuid
                        else -> null
                    }
                    response.add(
                        RecentItem(
                            id = index,
                            imageUrl = imageUrl,
                            title = title,
                            subtitle = subtitle,
                            uuid = itemId,
                            seriesUUID = seriesId,
                            itemType = itemType,
                            blurHash = item.blurHash
                        )
                    )
                }
                emit(Resource.success(response.toList()))
            }
        }
    }
}
