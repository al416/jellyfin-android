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
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomePage
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
class ObserveHomePage @Inject constructor(
    @Named("network") dispatcher: CoroutineDispatcher,
    private val getHomeSections: GetHomeSections,
    private val observeContinueWatchingSection: ObserveContinueWatchingSection,
    private val observeNextUpSection: ObserveNextUpSection,
    private val observeLatestSection: ObserveLatestSection,
    private val observeRecentItems: ObserveRecentItems
) : BaseUseCase<HomePage, ObserveHomePage.RequestParam?>(dispatcher) {

    override suspend fun invokeInternal(params: RequestParam?): Flow<Resource<HomePage>> {
        if (params == null) {
            throw IllegalArgumentException("Expecting valid parameters.")
        }

        return getHomeSections.invoke().flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<HomePage>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<HomePage>()) }
                }
                Status.SUCCESS -> {
                    loadAvailableSections(it.data ?: emptyList(), params.libraries, params.retrieveFromCache)
                }
            }
        }
    }

    private suspend fun loadAvailableSections(sections: List<HomeSectionType>, libraries: List<Library>, retrieveFromCache: Boolean): Flow<Resource<HomePage>> {
        return channelFlow<Resource<HomePage>> {
            send(Resource.loading())

            val flowNextUp = if (sections.contains(HomeSectionType.NEXT_UP)) {
                observeNextUpSection.invoke(ObserveNextUpSection.RequestParam(retrieveFromCache))
            } else {
                flow { emit(Resource.success(null)) }
            }

            val flowContinueWatching = if (sections.contains(HomeSectionType.CONTINUE_WATCHING)) {
                observeContinueWatchingSection.invoke(ObserveContinueWatchingSection.RequestParams(listOf("Video"), retrieveFromCache))
            } else {
                flow { emit(Resource.success(null)) }
            }

            val flowLatestItems = if (sections.contains(HomeSectionType.LATEST_MEDIA)) {
                loadLatest(libraries, retrieveFromCache)
            } else {
                flow { emit(Resource.success(null)) }
            }

            val flowRecentItems = observeRecentItems.invoke()

            val flowCombine = combine(
                flowNextUp,
                flowContinueWatching,
                flowLatestItems,
                flowRecentItems
            ) { nextUp, continueWatching, latestItemsFromMyMedia, recentItems ->
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
                    return@combine Resource.success(HomePage(recentItems.data ?: emptyList(), rows.toList()), errors)
                } else {
                    return@combine Resource.loading()
                }
            }

            flowCombine.collectLatest {
                send(it)
            }
        }
    }

    private suspend fun loadLatest(libraries: List<Library>, retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>> {
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

    data class RequestParam(val libraries: List<Library>, val retrieveFromCache: Boolean)
}
