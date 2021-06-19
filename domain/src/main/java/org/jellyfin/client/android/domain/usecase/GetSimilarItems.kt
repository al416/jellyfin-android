package org.jellyfin.client.android.domain.usecase


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.cached_model.CachedBaseItem
import org.jellyfin.client.android.domain.models.display_model.HomeCardAction
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.model.api.ImageType
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class GetSimilarItems @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                          private val viewsRepository: ViewsRepository,
                                          private val currentUserRepository: CurrentUserRepository,
                                          private val imageApi: ImageApi,
) : BaseUseCase<List<HomeSectionCard>, GetSimilarItems.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<HomeSectionCard>>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getSimilarItems(params.mediaId).flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<List<HomeSectionCard>>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<List<HomeSectionCard>>()) }
                }
                Status.SUCCESS -> {
                    processResults(it.data)
                }
            }
        }
    }

    private fun processResults(items: List<CachedBaseItem>?): Flow<Resource<List<HomeSectionCard>>> {
        return flow {
            if (items.isNullOrEmpty()) {
                emit (Resource.success<List<HomeSectionCard>>(emptyList()))
            } else {
                val response = mutableListOf<HomeSectionCard>()
                items.forEachIndexed { index, item ->
                    val imageUrl = imageApi.getItemImageUrl(
                        itemId = item.id,
                        imageType = ImageType.PRIMARY,
                        fillWidth = 500,
                        fillHeight = 500
                    )
                    val itemType = when (item.type) {
                        ItemType.MOVIE.type -> ItemType.MOVIE
                        ItemType.SERIES.type -> ItemType.SERIES
                        else -> ItemType.MOVIE
                    }
                    val card = HomeSectionCard(id = index + 1,
                        imageUrl = imageUrl,
                        title = item.name,
                        subtitle = null,
                        homeCardType = HomeCardType.POSTER,
                        uuid = item.id,
                        homeCardAction = HomeCardAction.DETAILS,
                        itemType = itemType,
                        blurHash = item.blurHash)
                    response.add(card)
                }
                emit(Resource.success(response.toList()))
            }
        }
    }

    data class RequestParams(val mediaId: UUID)
}
