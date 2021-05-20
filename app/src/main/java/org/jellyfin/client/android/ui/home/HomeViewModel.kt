package org.jellyfin.client.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.usecase.ObserveHomePage
import javax.inject.Inject
import javax.inject.Named

class HomeViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeHomePage: ObserveHomePage
) : ViewModel() {

    private val rows: MutableLiveData<Resource<List<HomeSectionRow>>> by lazy {
        val data = MutableLiveData<Resource<List<HomeSectionRow>>>()
        loadRows(data)
        data
    }

    fun getRows(): LiveData<Resource<List<HomeSectionRow>>> = rows

    private fun loadRows(data: MutableLiveData<Resource<List<HomeSectionRow>>>) {
        viewModelScope.launch(computationDispatcher) {
            observeHomePage.invoke().collectLatest {
                data.postValue(it)
            }
        }
    }

    fun refresh() {
        loadRows(rows)
    }
}