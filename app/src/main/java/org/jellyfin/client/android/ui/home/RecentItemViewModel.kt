package org.jellyfin.client.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.usecase.ObserveRecentItems
import javax.inject.Inject
import javax.inject.Named

class RecentItemViewModel @Inject constructor(
    @Named("computation") private val computationDispatcher: CoroutineDispatcher,
    private val observeRecentItems: ObserveRecentItems
) : ViewModel() {

    private val recentItems: MutableLiveData<Resource<List<HomeSectionCard>>> by lazy {
        val data = MutableLiveData<Resource<List<HomeSectionCard>>>()
        loadRecentItems(data)
        data
    }

    fun getRecentItems(): LiveData<Resource<List<HomeSectionCard>>> = recentItems

    private fun loadRecentItems(data: MutableLiveData<Resource<List<HomeSectionCard>>>) {
        viewModelScope.launch(computationDispatcher) {
            observeRecentItems.invoke().collectLatest {
                data.postValue(it)
            }
        }
    }
}