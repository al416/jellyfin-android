package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
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
                result.items?.forEach {item ->
                    // TODO: add items to response
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
                result.items?.forEach {item ->
                    // TODO: add items to response
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(Resource.error(listOf(Error(1, 1, "Error", null))))
            }
        }.flowOn(computationDispatcher)
    }
}