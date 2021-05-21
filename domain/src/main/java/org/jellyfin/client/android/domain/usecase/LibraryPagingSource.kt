package org.jellyfin.client.android.domain.usecase


import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.LIBRARY_PAGE_SIZE
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.repository.ViewsRepository
import javax.inject.Inject

class LibraryPagingSource @Inject constructor(private val viewsRepository: ViewsRepository
) : PagingSource<Int, HomeSectionCard>() {

    lateinit var library: Library
    private val pageSize = LIBRARY_PAGE_SIZE
    private var pageNumber = 0

    fun setParam(library: Library) {
        this.library = library
    }

    override fun getRefreshKey(state: PagingState<Int, HomeSectionCard>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HomeSectionCard> {
        return try {
            pageNumber = params.key ?: 0
            val result = viewsRepository.getLibraryItems(pageNumber, pageSize, library)
            val nextKey = if (result.size < pageSize) {
                null
            } else {
                pageNumber.plus(1)
            }
            LoadResult.Page(data = result, prevKey = null, nextKey = nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}