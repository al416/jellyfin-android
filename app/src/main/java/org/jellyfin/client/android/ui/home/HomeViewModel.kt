package org.jellyfin.client.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.LibraryDto
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

    private val libraries: MutableLiveData<Resource<List<LibraryDto>>> by lazy {
        val data = MutableLiveData<Resource<List<LibraryDto>>>()
        loadLibraries(data)
        data
    }

    fun getLibraries(): LiveData<Resource<List<LibraryDto>>> = libraries

    private fun loadLibraries(data: MutableLiveData<Resource<List<LibraryDto>>>) {
        viewModelScope.launch {
            observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParam(true)).collectLatest {
                data.postValue(it)
            }
        }
    }
}