package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.models.display_model.SeriesDetails
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class GetSeriesDetails @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                           private val viewsRepository: ViewsRepository,
                                           private val getSeasons: GetSeasons
) : BaseUseCase<SeriesDetails, GetSeriesDetails.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<SeriesDetails>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getSeriesDetails(params.seriesId).flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<SeriesDetails>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<SeriesDetails>()) }
                }
                Status.SUCCESS -> {
                    if (it.data == null) {
                        flow { emit(Resource.error<SeriesDetails>(it.messages)) }
                    } else {
                        getSeasons(params.seriesId, it.data)
                    }
                }
            }
        }
    }

    private suspend fun getSeasons(seriesId: UUID, seriesDetails: SeriesDetails): Flow<Resource<SeriesDetails>> {
        // TODO: Add another call in this chain to get the next episode and add it to the SeriesDetails object
        return getSeasons.invoke(GetSeasons.RequestParams(seriesId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<SeriesDetails>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<SeriesDetails>()) }
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        seriesDetails.seasons.addAll(it)
                    }
                    flow { emit(Resource.success(seriesDetails)) }
                }
            }
        }
    }

    data class RequestParams(val seriesId: UUID)
}