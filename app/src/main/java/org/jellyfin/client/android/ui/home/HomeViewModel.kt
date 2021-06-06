package org.jellyfin.client.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomePage
import org.jellyfin.client.android.domain.usecase.ObserveHomePage
import org.jellyfin.client.android.domain.usecase.ObserveMyMediaSection
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeHomePage: ObserveHomePage,
    private val observeMyMediaSection: ObserveMyMediaSection
) : ViewModel() {

    private val homePage: MediatorLiveData<Resource<HomePage>> by lazy {
        val data = MediatorLiveData<Resource<HomePage>>()
        data.addSource(libraries) { resource ->
            if (resource.status == Status.SUCCESS) {
                loadHomePage(data, resource.data ?: emptyList(), false)
            }
        }
        data
    }

    fun getHomePage(): LiveData<Resource<HomePage>> = homePage

    private fun loadHomePage(data: MediatorLiveData<Resource<HomePage>>, libraries: List<Library>, retrieveFromCache: Boolean) {
        viewModelScope.launch {
            observeHomePage.invoke(ObserveHomePage.RequestParam(libraries, retrieveFromCache)).collectLatest {
                data.postValue(it)
            }
        }
    }

    fun refresh(forceRefresh: Boolean) {
        loadLibraries(libraries, !forceRefresh)
    }

    fun clearErrors() {
        val data = homePage.value?.data
        homePage.postValue(Resource.Companion.success(data))
    }

    private val libraries: MutableLiveData<Resource<List<Library>>> by lazy {
        val data = MutableLiveData<Resource<List<Library>>>()
        loadLibraries(data, false)
        data
    }

    fun getLibraries(): LiveData<Resource<List<Library>>> = libraries

    private fun loadLibraries(data: MutableLiveData<Resource<List<Library>>>, retrieveFromCache: Boolean) {
        viewModelScope.launch {
            observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParam(retrieveFromCache)).collectLatest {
                data.postValue(it)
            }
        }
    }
}
