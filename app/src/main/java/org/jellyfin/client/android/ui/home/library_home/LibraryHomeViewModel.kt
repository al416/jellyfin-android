package org.jellyfin.client.android.ui.home.library_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.domain.usecase.GetGenres
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
class LibraryHomeViewModel
@Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val getGenres: GetGenres
) : ViewModel() {

    private lateinit var library: Library

    fun initialize(library: Library) {
        this.library = library
    }

    private val genres: MutableLiveData<Resource<List<Genre>>> by lazy {
        val data = MutableLiveData<Resource<List<Genre>>>()
        loadGenres(data)
        data
    }

    fun getGenres(): LiveData<Resource<List<Genre>>> = genres

    private fun loadGenres(data: MutableLiveData<Resource<List<Genre>>>) {
        viewModelScope.launch(computationDispatcher) {
            getGenres.invoke(GetGenres.RequestParams(libraryId = library.uuid, itemType = library.type)).collectLatest {
                data.postValue(it)
            }
        }
    }
}