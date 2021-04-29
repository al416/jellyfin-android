package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
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
                                          private val observeLatestSection: ObserveLatestSection) : BaseUseCase<List<HomeSectionRow>, ObserveHomePage.RequestParams>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParams?): Flow<Resource<List<HomeSectionRow>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters")
        }

        return getHomeSections.invoke(GetHomeSections.RequestParams(params.userId)).flatMapLatest {
            when (it.status) {
                Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(it.messages)) }
                Status.LOADING -> flow {emit(Resource.loading<List<HomeSectionRow>>())}
                Status.SUCCESS -> {
                    if (it.data.isNullOrEmpty()) {
                        // TODO: Use correct error
                        flow { emit(Resource.error<List<HomeSectionRow>>(listOf(Error(0, 0, "Could not retrieve home sections", null)))) }
                    } else {
                        val rows = mutableListOf<HomeSectionRow>()
                        loadMyMedia(params, it.data, rows)
                    }
                }
            }
        }
    }

    private suspend fun loadMyMedia(params: RequestParams, sections: List<HomeSectionType>,
                                    rows: MutableList<HomeSectionRow>): Flow<Resource<List<HomeSectionRow>>> {
        val libraries = mutableListOf<LibraryDto>()
        if (sections.contains(HomeSectionType.MY_MEDIA)) {
            return observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParams(params.userId))
                .flatMapLatest { resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> {
                            resource.data?.let {
                                // Only add a row if the row has cards
                                if (it.cards.isNotEmpty()) {
                                    rows.add(it)
                                    it.cards.forEach {card ->
                                        libraries.add(LibraryDto(id = card.uuid, title = card.title))
                                    }
                                }
                            }
                            loadContinueWatching(params, sections, rows, libraries)
                        }
                    }
                }
        } else {
            return loadContinueWatching(params, sections, rows, libraries)
        }
    }

    private suspend fun loadContinueWatching(params: RequestParams, sections: List<HomeSectionType>,
                                             rows: MutableList<HomeSectionRow>,
                                             libraries: List<LibraryDto>): Flow<Resource<List<HomeSectionRow>>> {
        if (sections.contains(HomeSectionType.CONTINUE_WATCHING)) {
            return observeContinueWatchingSection.invoke(ObserveContinueWatchingSection.RequestParams(params.userId, listOf("Video")))
                .flatMapLatest { resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> {
                            resource.data?.let {
                                if (it.cards.isNotEmpty()) {
                                    rows.add(it)
                                }
                            }
                            loadNextUp(params, sections, rows, libraries)
                        }
                    }
                }
        } else {
            return loadNextUp(params, sections, rows, libraries)
        }
    }

    private suspend fun loadNextUp(params: RequestParams, sections: List<HomeSectionType>,
                                   rows: MutableList<HomeSectionRow>,
                                   libraries: List<LibraryDto>): Flow<Resource<List<HomeSectionRow>>> {
        if (sections.contains(HomeSectionType.NEXT_UP)) {
            return observeNextUpSection.invoke(ObserveNextUpSection.RequestParams(params.userId))
                .flatMapLatest {resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> {
                            resource.data?.let {
                                if (it.cards.isNotEmpty()) {
                                    rows.add(it)
                                }
                            }
                            loadLatest(params, sections, rows, libraries)
                        }
                    }
                }
        } else {
            return loadLatest(params, sections, rows, libraries)
        }
    }

    private suspend fun loadLatest(params: RequestParams, sections: List<HomeSectionType>,
                                   rows: MutableList<HomeSectionRow>,
                                   libraries: List<LibraryDto>): Flow<Resource<List<HomeSectionRow>>> {
        if (sections.contains(HomeSectionType.LATEST_MEDIA)) {
            return observeLatestSection.invoke(ObserveLatestSection.RequestParams(params.userId, libraries))
                .flatMapLatest {resource ->
                    when (resource.status) {
                        Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                        Status.LOADING -> flow {
                            // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                        }
                        Status.SUCCESS -> flow {
                            resource.data?.let {
                                if (it.isNotEmpty()) {
                                    rows.addAll(it)
                                }
                            }
                            emit(Resource.success(rows.toList()))
                        }
                    }
                }
        } else {
            return flow { emit(Resource.success(rows.toList())) }
        }
    }

    data class RequestParams(val userId: UUID)
}