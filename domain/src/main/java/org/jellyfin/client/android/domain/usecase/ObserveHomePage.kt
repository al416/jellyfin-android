package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.LibraryDto
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
class ObserveHomePage @Inject constructor(
    @Named("network") dispatcher: CoroutineDispatcher,
    private val getHomeSections: GetHomeSections,
    private val observeMyMediaSection: ObserveMyMediaSection,
    private val observeContinueWatchingSection: ObserveContinueWatchingSection,
    private val observeNextUpSection: ObserveNextUpSection,
    private val observeLatestSection: ObserveLatestSection
) : BaseUseCase<List<HomeSectionRow>, ObserveHomePage.RequestParam?>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParam?): Flow<Resource<List<HomeSectionRow>>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters.")
        }

        return channelFlow<Resource<List<HomeSectionRow>>> {
            send(Resource.loading())

            val flowNextUp = observeNextUpSection.invoke(ObserveNextUpSection.RequestParam(params.retrieveFromCache))
            val flowContinueWatching = observeContinueWatchingSection.invoke(ObserveContinueWatchingSection.RequestParams(listOf("Video"), params.retrieveFromCache))
            val flowLatestItems = loadLatestItemsFromMyMedia(params.retrieveFromCache)

            val flowCombine = combine(
                flowNextUp,
                flowContinueWatching,
                flowLatestItems
            ) { nextUp, continueWatching, latestItemsFromMyMedia ->
                // If all the sections have finished loading (either successfully or with errors) then emit the list of rows
                if (nextUp.status != Status.LOADING && continueWatching.status != Status.LOADING && latestItemsFromMyMedia.status != Status.LOADING) {
                    val rows = mutableListOf<HomeSectionRow>()
                    continueWatching.data?.let {
                        rows.add(it)
                    }
                    nextUp.data?.let {
                        rows.add(it)
                    }
                    latestItemsFromMyMedia.data?.let {
                        rows.addAll(it)
                    }
                    val errors = mutableListOf<Error>()
                    continueWatching.messages?.let {
                        errors.addAll(it)
                    }
                    nextUp.messages?.let {
                        errors.addAll(it)
                    }
                    latestItemsFromMyMedia.messages?.let {
                        errors.addAll(it)
                    }
                    return@combine Resource.success(rows.toList(), errors)
                } else {
                    return@combine Resource.loading()
                }
            }

            flowCombine.collectLatest {
                send(it)
            }
        }
    }

    private suspend fun loadLatestItemsFromMyMedia(retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>> {
        return observeMyMediaSection.invoke(ObserveMyMediaSection.RequestParam(retrieveFromCache))
            .flatMapLatest { resource ->
                when (resource.status) {
                    Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                    Status.LOADING -> flow {
                        // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                    }
                    Status.SUCCESS -> {
                        val libraries = mutableListOf<LibraryDto>()
                        resource.data?.let {
                            it.cards.forEach { card ->
                                libraries.add(LibraryDto(id = card.uuid, title = card.title))
                            }
                        }
                        loadLatest(libraries, retrieveFromCache)
                    }
                }
            }
    }

    private suspend fun loadLatest(libraries: List<LibraryDto>, retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>> {
        return observeLatestSection.invoke(ObserveLatestSection.RequestParams(libraries, retrieveFromCache))
            .flatMapLatest { resource ->
                when (resource.status) {
                    Status.ERROR -> flow { emit(Resource.error<List<HomeSectionRow>>(resource.messages)) }
                    Status.LOADING -> flow {
                        // Don't emit another LOADING resource because the first request in the chain already emitted a LOADING resource
                    }
                    Status.SUCCESS -> flow {
                        val rows = mutableListOf<HomeSectionRow>()
                        resource.data?.let {
                            if (it.isNotEmpty()) {
                                rows.addAll(it)
                            }
                        }
                        emit(Resource.success(rows.toList()))
                    }
                }
            }
    }

    data class RequestParam(val retrieveFromCache: Boolean)
}