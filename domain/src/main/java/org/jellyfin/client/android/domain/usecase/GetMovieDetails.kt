package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.repository.ViewsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class GetMovieDetails @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                            private val viewsRepository: ViewsRepository
) : BaseUseCase<MovieDetails, GetMovieDetails.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<MovieDetails>> {
        if (params == null) {
            throw IllegalArgumentException("No parameters passed! Please pass the required parameters.")
        }

        return viewsRepository.getMovieDetails(params.movieId)
    }

    data class RequestParams(val movieId: UUID)
}