package org.jellyfin.client.android.ui.home.series_details


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.SeriesDetails
import org.jellyfin.client.android.domain.usecase.GetSeriesDetails
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class SeriesDetailsViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val getSeriesDetails: GetSeriesDetails
) : ViewModel() {

    private lateinit var seriesId: UUID

    fun initialize(id: UUID) {
        seriesId = id
    }

    private val seriesDetails: MutableLiveData<Resource<SeriesDetails>> by lazy {
        val data = MutableLiveData<Resource<SeriesDetails>>()
        loadSeriesDetails(data)
        data
    }

    fun getSeriesDetails(): LiveData<Resource<SeriesDetails>> = seriesDetails

    private fun loadSeriesDetails(data: MutableLiveData<Resource<SeriesDetails>>) {
        viewModelScope.launch(computationDispatcher) {
            getSeriesDetails.invoke(GetSeriesDetails.RequestParams(seriesId)).collectLatest {
                data.postValue(it)
            }
        }
    }
}