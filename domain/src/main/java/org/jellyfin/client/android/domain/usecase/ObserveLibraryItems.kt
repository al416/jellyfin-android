package org.jellyfin.client.android.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.LIBRARY_PAGE_SIZE
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.repository.ViewsRepository
import javax.inject.Inject

class ObserveLibraryItems @Inject constructor(private val viewsRepository: ViewsRepository) {

    fun invokeInternal(params: RequestParams?): Flow<PagingData<HomeSectionCard>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        val pagingSource = LibraryPagingSource(viewsRepository)
        pagingSource.setParam(params.library, params.genre)

        val pagingConfig = PagingConfig(pageSize = LIBRARY_PAGE_SIZE, initialLoadSize = LIBRARY_PAGE_SIZE * 2, enablePlaceholders = false)

        return Pager(config = pagingConfig, pagingSourceFactory = {pagingSource}).flow
    }

    data class RequestParams(val library: Library?, val genre: Genre)
}
