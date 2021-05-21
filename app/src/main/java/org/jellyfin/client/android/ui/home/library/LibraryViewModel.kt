package org.jellyfin.client.android.ui.home.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.usecase.ObserveLibraryItems
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class LibraryViewModel
@Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeLibraryItems: ObserveLibraryItems
) : ViewModel() {

    private lateinit var library: Library
    private var job: Job? = null

    fun initialize(library: Library) {
        this.library = library
    }

    private val items: MutableLiveData<PagingData<HomeSectionCard>> by lazy {
        val data = MutableLiveData<PagingData<HomeSectionCard>>()
        startJob(data)
        data
    }

    fun getItems(): LiveData<PagingData<HomeSectionCard>> = items

    private fun loadItems(): Flow<PagingData<HomeSectionCard>> {
        val result: Flow<PagingData<HomeSectionCard>> = observeLibraryItems.invokeInternal(ObserveLibraryItems.RequestParams(library))
            .cachedIn(viewModelScope)
        return result
    }

    private fun startJob(data: MutableLiveData<PagingData<HomeSectionCard>>) {
        job?.cancel()
        job = viewModelScope.launch {
            loadItems().collectLatest {
                data.postValue(it)
            }
        }
    }
}