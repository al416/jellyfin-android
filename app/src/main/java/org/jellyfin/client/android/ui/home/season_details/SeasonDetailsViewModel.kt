package org.jellyfin.client.android.ui.home.season_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Episode
import org.jellyfin.client.android.domain.usecase.GetEpisodes
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class SeasonDetailsViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val getEpisodes: GetEpisodes
) : ViewModel() {

    private lateinit var seriesId: UUID
    private lateinit var seasonId: UUID

    fun initialize(seriesId: UUID, seasonId: UUID) {
        this.seriesId = seriesId
        this.seasonId = seasonId
    }

    private val seasonDetails: MutableLiveData<Resource<List<Episode>>> by lazy {
        val data = MutableLiveData<Resource<List<Episode>>>()
        loadSeasonDetails(data)
        data
    }

    fun getSeasonDetails(): LiveData<Resource<List<Episode>>> = seasonDetails

    private fun loadSeasonDetails(data: MutableLiveData<Resource<List<Episode>>>) {
        viewModelScope.launch(computationDispatcher) {
            getEpisodes.invoke(GetEpisodes.RequestParams(seriesId, seasonId)).collectLatest {
                data.postValue(it)
            }
        }
    }
}