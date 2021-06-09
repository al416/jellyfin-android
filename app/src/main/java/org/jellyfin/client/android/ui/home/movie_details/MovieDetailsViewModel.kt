package org.jellyfin.client.android.ui.home.movie_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.usecase.GetMovieDetails
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MovieDetailsViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val getMovieDetails: GetMovieDetails
) : ViewModel() {

    private lateinit var movieId: UUID

    fun initialize(id: UUID) {
        movieId = id
    }

    private val movieDetails: MutableLiveData<Resource<MovieDetails>> by lazy {
        val data = MutableLiveData<Resource<MovieDetails>>()
        loadMovieDetails(data)
        data
    }

    fun getMovieDetails(): LiveData<Resource<MovieDetails>> = movieDetails

    private fun loadMovieDetails(data: MutableLiveData<Resource<MovieDetails>>) {
        viewModelScope.launch(computationDispatcher) {
            getMovieDetails.invoke(GetMovieDetails.RequestParams(movieId)).collectLatest {
                data.postValue(it)
            }
        }
    }

    fun refresh() {
        loadMovieDetails(movieDetails)
    }
}
