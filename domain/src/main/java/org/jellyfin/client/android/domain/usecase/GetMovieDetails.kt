package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class GetMovieDetails @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                          private val viewsRepository: ViewsRepository,
                                          private val getSimilarItems: GetSimilarItems
) : BaseUseCase<MovieDetails, GetMovieDetails.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<MovieDetails>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getMovieDetails(params.movieId).flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<MovieDetails>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<MovieDetails>()) }
                }
                Status.SUCCESS -> {
                    if (it.data == null) {
                        flow { emit(Resource.error<MovieDetails>(it.messages)) }
                    } else {
                        getSimilarItems(params.movieId, it.data)
                    }
                }
            }
        }
    }

    private suspend fun getSimilarItems(movieId: UUID, movieDetails: MovieDetails): Flow<Resource<MovieDetails>> {
        return getSimilarItems.invoke(GetSimilarItems.RequestParams(movieId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<MovieDetails>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<MovieDetails>()) }
                }
                Status.SUCCESS -> {
                    if (it.data == null) {
                        flow { emit(Resource.error<MovieDetails>(it.messages)) }
                    } else {
                        val similarItems = it.data
                        movieDetails.similarItems = HomeSectionRow(1, "More Like This", similarItems)
                        flow { emit(Resource.success(movieDetails)) }
                    }
                }
            }
        }
    }

    data class RequestParams(val movieId: UUID)
}
