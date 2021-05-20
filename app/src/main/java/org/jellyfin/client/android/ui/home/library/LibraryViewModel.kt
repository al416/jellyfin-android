package org.jellyfin.client.android.ui.home.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.usecase.ObserveLibraryItems
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class LibraryViewModel
@Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeLibraryItems: ObserveLibraryItems
) : ViewModel() {

    private lateinit var libraryId: UUID

    fun initialize(libraryId: String) {
        this.libraryId = UUID.fromString(libraryId)
    }

    private val items: MutableLiveData<Resource<List<HomeSectionCard>>> by lazy {
        val data = MutableLiveData<Resource<List<HomeSectionCard>>>()
        loadItems(data)
        data
    }

    fun getItems(): LiveData<Resource<List<HomeSectionCard>>> = items

    private fun loadItems(data: MutableLiveData<Resource<List<HomeSectionCard>>>) {
        viewModelScope.launch {
            observeLibraryItems.invoke(ObserveLibraryItems.RequestParams(libraryId)).collectLatest {
                data.postValue(it)
            }
        }
    }
}