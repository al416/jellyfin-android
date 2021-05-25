package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.models.display_model.SeriesDetails
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

        return getHomeSections.invoke().flatMapLatest {
            when (it.status) {
                Status.ERROR -> {
                    flow { emit(Resource.error<List<HomeSectionRow>>(it.messages)) }
                }
                Status.LOADING -> {
                    flow { emit(Resource.loading<List<HomeSectionRow>>()) }
                }
                Status.SUCCESS -> {
                    loadAvailableSections(it.data ?: emptyList(), params.retrieveFromCache)
                }
            }
        }
    }

    private suspend fun loadAvailableSections(sections: List<HomeSectionType>, retrieveFromCache: Boolean): Flow<Resource<List<HomeSectionRow>>> {
        // This branch contains code to allow home page requests to be submitted in parallel and asynchronously
        // After each request is complete, the request flow should send(Resource.success) with the row which will be collected
        // by the StateFlow in the ViewModel.
        // TODO: Figure out the model that should be sent to HomeViewModel
        return channelFlow<Resource<List<HomeSectionRow>>> {
            send(Resource.loading())

            val flowNextUp = if (sections.contains(HomeSectionType.NEXT_UP)) {
                val rowId = sections.indexOf(HomeSectionType.NEXT_UP)
                observeNextUpSection.invoke(ObserveNextUpSection.RequestParam(rowId, retrieveFromCache))
            } else {
                flow { emit(Resource.success(null)) }
            }

            val test = async {
                flowNextUp.collectLatest {
                    println("JELLYDEBUG flowNextUp $it")
                    send(Resource.loading())
                }
            }

            val flowContinueWatching = if (sections.contains(HomeSectionType.CONTINUE_WATCHING)) {
                observeContinueWatchingSection.invoke(ObserveContinueWatchingSection.RequestParams(listOf("Video"), retrieveFromCache))
            } else {
                flow { emit(Resource.success(null)) }
            }

            // The async allows the request to run in parallel
            val test2 = async {
                flowContinueWatching.collectLatest {
                    println("JELLYDEBUG flowContinueWatching $it")
                    send(Resource.loading())
                }
            }

            val flowLatestItems = loadLatestItemsFromMyMedia(retrieveFromCache)

            flowLatestItems.collectLatest {
                println("JELLYDEBUG flowLatestItems $it")
                send(Resource.loading())
            }
            /*

            val flowCombine = combine(
                flowNextUp,
                flowContinueWatching,
                flowLatestItems
            ) { nextUp, continueWatching, latestItemsFromMyMedia ->
                println("JELLYDEBUG Combine called nextUp $nextUp")
                println("JELLYDEBUG Combine called continueWatching $continueWatching")
                println("JELLYDEBUG Combine called latestItemsFromMyMedia $latestItemsFromMyMedia")
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
                    flow { emit(Resource.success(null)) }
                    println("JELLYDEBUG Combine success")
                    return@combine Resource.success(rows.toList(), errors)
                } else {
                    println("JELLYDEBUG Combine loading")
                    return@combine Resource.loading()
                }
            }.collect {
                println("JELLYDEBUG Combine inside collectlatest")
                send(it)
            }


             */


            awaitClose()
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
                        val libraries = resource.data ?: emptyList()
                        loadLatest(libraries, retrieveFromCache)
                    }
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

    data class RequestParam(val retrieveFromCache: Boolean)
}