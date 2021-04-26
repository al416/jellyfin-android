package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeContents
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class ObserveHomePage @Inject constructor(@Named("network") dispatcher: CoroutineDispatcher,
                                          private val getHomeSections: GetHomeSections,
                                          private val observeMyMediaSection: ObserveMyMediaSection,
                                          private val observeContinueWatchingSection: ObserveContinueWatchingSection,
                                          private val observeNextUpSection: ObserveNextUpSection,
                                          private val observeLatestSection: ObserveLatestSection) : BaseUseCase<HomeContents, ObserveHomePage.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<HomeContents>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return getHomeSections.invoke(GetHomeSections.RequestParams(params.userId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> flow { emit(Resource.error<HomeContents>(it.messages)) }
                Status.LOADING -> flow {emit(Resource.loading<HomeContents>())}
                Status.SUCCESS -> {
                    if (it.data.isNullOrEmpty()) {
                        // TODO: Use correct error
                        flow { emit(Resource.error<HomeContents>(listOf(Error(0, 0, "Could not retrieve home sections", null)))) }
                    } else {
                        val rows = mutableListOf<HomeSectionRow>()
                        val cards= mutableListOf<HomeSectionCard>()
                        loadMyMedia(params, it.data, rows, cards)
                    }
                }
            }
        }
    }

    private suspend fun loadMyMedia(params: RequestParams, sections: List<HomeSectionType>,
                                    rows: MutableList<HomeSectionRow>, cards: MutableList<HomeSectionCard>): Flow<Resource<HomeContents>> {
        if (sections.contains(HomeSectionType.MY_MEDIA)) {
            val rowId = sections.indexOf(HomeSectionType.MY_MEDIA)
            return observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParams(params.userId))
                .flatMapLatest { resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<HomeContents>(resource.messages)) }
                        Status.LOADING -> flow {emit(Resource.loading<HomeContents>())}
                        Status.SUCCESS -> {
                            resource.data?.let {resultCards ->
                                resultCards.forEach { it.rowId = rowId }
                                cards.addAll(resultCards)
                            }
                            val libraryIds = mutableListOf<UUID>()
                            resource.data?.forEach {
                                libraryIds.add(it.uuid)
                            }
                            rows.add(HomeSectionRow(id = rowId, title = "My Media"))
                            loadContinueWatching(params, sections, rows, cards, libraryIds)
                        }
                    }
                }
        } else {
            val libraryIds = mutableListOf<UUID>()
            return loadContinueWatching(params, sections, rows, cards, libraryIds)
        }
    }

    private suspend fun loadContinueWatching(params: RequestParams, sections: List<HomeSectionType>,
                                             rows: MutableList<HomeSectionRow>, cards: MutableList<HomeSectionCard>,
                                             libraryIds: List<UUID>): Flow<Resource<HomeContents>> {
        if (sections.contains(HomeSectionType.CONTINUE_WATCHING)) {
            val rowId = sections.indexOf(HomeSectionType.CONTINUE_WATCHING)
            return observeContinueWatchingSection.invoke(ObserveContinueWatchingSection.RequestParams(params.userId, listOf("Video")))
                .flatMapLatest { resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<HomeContents>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> {
                            resource.data?.let {resultCards ->
                                resultCards.forEach { it.rowId = rowId }
                                cards.addAll(resultCards)
                            }
                            rows.add(HomeSectionRow(id = rowId, title = "Continue Watching"))
                            loadNextUp(params, sections, rows, cards, libraryIds)
                        }
                    }
                }
        } else {
            return loadNextUp(params, sections, rows, cards, libraryIds)
        }
    }

    private suspend fun loadNextUp(params: RequestParams, sections: List<HomeSectionType>,
                                   rows: MutableList<HomeSectionRow>, cards: MutableList<HomeSectionCard>,
                                   libraryIds: List<UUID>): Flow<Resource<HomeContents>> {
        if (sections.contains(HomeSectionType.NEXT_UP)) {
            val rowId = sections.indexOf(HomeSectionType.NEXT_UP)
            return observeNextUpSection.invoke(ObserveNextUpSection.RequestParams(params.userId))
                .flatMapLatest {resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<HomeContents>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> {
                            resource.data?.let {resultCards ->
                                resultCards.forEach { it.rowId = rowId }
                                cards.addAll(resultCards)
                            }
                            rows.add(HomeSectionRow(id = rowId, title = "Next Up"))
                            loadLatest(params, sections, rows, cards, libraryIds)
                        }
                    }
                }
        } else {
            return loadLatest(params, sections, rows, cards, libraryIds)
        }
    }

    private suspend fun loadLatest(params: RequestParams, sections: List<HomeSectionType>,
                                   rows: MutableList<HomeSectionRow>, cards: MutableList<HomeSectionCard>,
                                   libraryIds: List<UUID>): Flow<Resource<HomeContents>> {
        if (sections.contains(HomeSectionType.LATEST_MEDIA)) {
            val rowId = sections.indexOf(HomeSectionType.CONTINUE_WATCHING)
            return observeLatestSection.invoke(ObserveLatestSection.RequestParams(params.userId, libraryIds))
                .flatMapLatest {resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<HomeContents>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> flow {
                            resource.data?.let {resultCards ->
                                resultCards.forEach { it.rowId = rowId }
                                cards.addAll(resultCards)
                            }
                            rows.add(HomeSectionRow(id = rowId, title = "Latest Media"))
                            emit(Resource.success(HomeContents(rows.toList(), cards.toList())))
                        }
                    }
                }
        } else {
            return flow { emit(Resource.success(HomeContents(rows.toList(), cards.toList()))) }
        }
    }

    data class RequestParams(val userId: UUID)
}