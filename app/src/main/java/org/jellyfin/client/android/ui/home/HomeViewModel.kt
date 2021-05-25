package org.jellyfin.client.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.usecase.ObserveHomePage
import org.jellyfin.client.android.domain.usecase.ObserveMyMediaSection
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class HomeViewModel
@Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeHomePage: ObserveHomePage,
    private val observeMyMediaSection: ObserveMyMediaSection
) : ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow(Resource.loading<List<HomeSectionRow>>())
    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<Resource<List<HomeSectionRow>>> = _uiState

    init {
        viewModelScope.launch {
            observeHomePage.invoke(ObserveHomePage.RequestParam(false)).collectLatest {
                println("JELLYDEBUG observing home page inside HomeViewModel. observeHomePage is $it")
                _uiState.value = it
            }
        }
    }

    private val rows: MutableLiveData<Resource<List<HomeSectionRow>>> by lazy {
        val data = MutableLiveData<Resource<List<HomeSectionRow>>>()
        loadRows(data, false)
        data
    }

    fun getRows(): LiveData<Resource<List<HomeSectionRow>>> = rows

    private fun loadRows(data: MutableLiveData<Resource<List<HomeSectionRow>>>, retrieveFromCache: Boolean) {
        // TODO: Both loadRows and loadLibraries call observeMyMediaSection to get the list of libraries
        // Since both loadRows and loadLibraries are executed around the same time, this call is made to the server twice
        // Code needs to be updated so the call is made only once
        viewModelScope.launch {
            observeHomePage.invoke(ObserveHomePage.RequestParam(retrieveFromCache)).collectLatest {
                println("JELLYDEBUG observing home page inside HomeViewModel. observeHomePage is $it")
                data.postValue(it)
            }
        }
    }

    fun refresh(forceRefresh: Boolean) {
        loadRows(rows, !forceRefresh)
    }

    fun clearErrors() {
        val data = rows.value?.data
        rows.postValue(Resource.Companion.success(data))
    }

    private val libraries: MutableLiveData<Resource<List<Library>>> by lazy {
        val data = MutableLiveData<Resource<List<Library>>>()
        loadLibraries(data)
        data
    }

    fun getLibraries(): LiveData<Resource<List<Library>>> = libraries

    private fun loadLibraries(data: MutableLiveData<Resource<List<Library>>>) {
        viewModelScope.launch {
            observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParam(true)).collectLatest {
                data.postValue(it)
            }
        }
    }
}