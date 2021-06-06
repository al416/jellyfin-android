package org.jellyfin.client.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.constants.CollectionType
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.RECENT_ITEMS_MAX_COUNT
import org.jellyfin.client.android.domain.constants.Constants.GENRE_ALL_ID
import org.jellyfin.client.android.domain.constants.PersonType
import org.jellyfin.client.android.domain.models.Error
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.cached_model.CachedRecentItem
import org.jellyfin.client.android.domain.models.display_model.Episode
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.domain.models.display_model.HomeCardAction
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow
import org.jellyfin.client.android.domain.models.display_model.HomeSectionType
import org.jellyfin.client.android.domain.models.display_model.MovieDetails
import org.jellyfin.client.android.domain.models.display_model.Person
import org.jellyfin.client.android.domain.models.display_model.Season
import org.jellyfin.client.android.domain.models.display_model.SeriesDetails
import org.jellyfin.client.android.domain.repository.CurrentUserRepository
import org.jellyfin.client.android.domain.repository.ViewsRepository
import org.jellyfin.sdk.api.operations.DisplayPreferencesApi
import org.jellyfin.sdk.api.operations.GenresApi
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.api.operations.UserLibraryApi
import org.jellyfin.sdk.api.operations.UserViewsApi
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemFilter
import org.jellyfin.sdk.model.api.SortOrder
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class ViewsRepositoryImpl @Inject constructor(
    @Named("network") private val networkDispatcher: CoroutineDispatcher,
    private val userViewsApi: UserViewsApi,
    private val itemsApi: ItemsApi,
    private val tvShowsApi: TvShowsApi,
    private val userLibraryApi: UserLibraryApi,
    private val imageApi: ImageApi,
    private val genresApi: GenresApi,
    private val displayPreferencesApi: DisplayPreferencesApi,
    private val currentUserRepository: CurrentUserRepository
) : ViewsRepository {

    private val homeRowCache = mutableMapOf<String, HomeSectionRow>()
    private val libraryCache = mutableMapOf<String, List<Library>>()

    override suspend fun getMyMediaSection(retrieveFromCache: Boolean): Flow<Resource<List<Library>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<Library>>> {
            emit(Resource.loading())
            val row = libraryCache[HomeSectionType.MY_MEDIA.name]
            if (retrieveFromCache && row != null) {
                emit(Resource.success(row))
            } else {
                try {
                    val libraries = mutableListOf<Library>()
                    val result by userViewsApi.getUserViews(userId)
                    val filteredResults = result.items?.filter { it.collectionType == CollectionType.MOVIES || it.collectionType == CollectionType.TV_SHOWS }
                    filteredResults?.forEachIndexed { index, baseItemDto ->
                        val type = if (baseItemDto.collectionType == CollectionType.MOVIES) ItemType.MOVIE else ItemType.SERIES
                        libraries.add(Library(id = index, uuid = baseItemDto.id, title = baseItemDto.name, type = type))
                    }
                    libraryCache[HomeSectionType.MY_MEDIA.name] = libraries
                    emit(Resource.success(libraries))
                } catch (e: Exception) {
                    val error = Error(null, 1,"Could not load Libraries ${e.message}", null)
                    emit(Resource.error(listOf(error)))
                }
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getContinueWatchingSection(
        mediaTypes: List<String>?,
        retrieveFromCache: Boolean
    ): Flow<Resource<HomeSectionRow>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<HomeSectionRow>> {
            emit(Resource.loading())
            val row = homeRowCache[HomeSectionType.CONTINUE_WATCHING.name]
            if (retrieveFromCache && row != null) {
                emit(Resource.success(row))
            } else {
                try {
                    val cards = mutableListOf<HomeSectionCard>()
                    val result by itemsApi.getResumeItems(
                        userId = userId,
                        limit = 12,
                        fields = listOf(
                            ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                            ItemFields.BASIC_SYNC_INFO
                        ),
                        imageTypeLimit = 1,
                        enableImageTypes = listOf(
                            ImageType.PRIMARY,
                            ImageType.BACKDROP,
                            ImageType.THUMB
                        ),
                        mediaTypes = mediaTypes
                    )
                    result.items?.forEachIndexed { index, item ->
                        var itemId = item.id
                        if (item.backdropImageTags.isNullOrEmpty() && !item.parentBackdropItemId.isNullOrBlank() && item.seriesId != null) {
                            itemId = item.seriesId!!
                        }
                        val imageUrl = imageApi.getItemImageUrl(
                            itemId = itemId,
                            imageType = ImageType.BACKDROP
                        )
                        val itemType = when (item.type) {
                            ItemType.MOVIE.type -> ItemType.MOVIE
                            ItemType.SERIES.type -> ItemType.SERIES
                            else -> ItemType.MOVIE
                        }
                        val map = if (item.imageBlurHashes.containsKey(ImageType.BACKDROP)) item.imageBlurHashes[ImageType.BACKDROP] else null
                        val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                        cards.add(
                            HomeSectionCard(
                                id = index,
                                imageUrl = imageUrl,
                                title = item.name,
                                subtitle = null,
                                uuid = item.id,
                                homeCardType = HomeCardType.BACKDROP,
                                homeCardAction = HomeCardAction.PLAY,
                                itemType = itemType,
                                blurHash = blurHash
                            )
                        )
                    }
                    // TODO: Use a repo to get Continue Watching string
                    if (cards.isEmpty()) {
                        emit(Resource.success(null))
                    } else {
                        val row = HomeSectionRow(
                            id = HomeSectionType.CONTINUE_WATCHING.ordinal,
                            title = "Continue Watching",
                            cards = cards
                        )
                        homeRowCache[HomeSectionType.CONTINUE_WATCHING.name] = row
                        emit(Resource.success(row))
                    }
                } catch (e: Exception) {
                    // TODO: Need to catch httpException and pass along correct error message
                    val error = e.message
                    emit(
                        Resource.error(
                            listOf(
                                Error(
                                    1,
                                    1,
                                    "Could not load Continue Watching section $error",
                                    null
                                )
                            )
                        )
                    )
                }
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getNextUpSection(retrieveFromCache: Boolean): Flow<Resource<HomeSectionRow>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<HomeSectionRow>> {
            emit(Resource.loading())
            val row = homeRowCache[HomeSectionType.NEXT_UP.name]
            if (retrieveFromCache && row != null) {
                emit(Resource.success(row))
            } else {
                try {
                    val cards = mutableListOf<HomeSectionCard>()
                    val result by tvShowsApi.getNextUp(
                        userId = userId,
                        limit = 24,
                        fields = listOf(
                            ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                            ItemFields.BASIC_SYNC_INFO
                        ),
                        imageTypeLimit = 1,
                        enableImageTypes = listOf(
                            ImageType.PRIMARY,
                            ImageType.BACKDROP,
                            ImageType.BANNER,
                            ImageType.THUMB
                        ),
                        disableFirstEpisode = true
                    )
                    result.items?.forEachIndexed { index, item ->
                        val imageUrl = imageApi.getItemImageUrl(
                            itemId = item.id,
                            imageType = ImageType.BACKDROP
                        )
                        val itemType = when (item.type) {
                            ItemType.MOVIE.type -> ItemType.MOVIE
                            ItemType.SERIES.type -> ItemType.SERIES
                            else -> ItemType.MOVIE
                        }
                        val map = if (item.imageBlurHashes.containsKey(ImageType.BACKDROP)) item.imageBlurHashes[ImageType.BACKDROP] else null
                        val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                        cards.add(
                            HomeSectionCard(
                                id = index,
                                imageUrl = imageUrl,
                                title = item.name,
                                subtitle = null,
                                uuid = item.id,
                                homeCardType = HomeCardType.BACKDROP,
                                homeCardAction = HomeCardAction.PLAY,
                                itemType = itemType,
                                blurHash = blurHash
                            )
                        )
                    }
                    // TODO: Use a repo to get Next Up string
                    if (cards.isEmpty()) {
                        emit(Resource.success(null))
                    } else {
                        val row = HomeSectionRow(
                            id = HomeSectionType.NEXT_UP.ordinal,
                            title = "Next Up",
                            cards = cards
                        )
                        homeRowCache[HomeSectionType.NEXT_UP.name] = row
                        emit(Resource.success(row))
                    }
                } catch (e: Exception) {
                    // TODO: Need to catch httpException and pass along correct error message
                    val error = e.message
                    emit(
                        Resource.error(
                            listOf(
                                Error(
                                    1,
                                    1,
                                    "Could not load Next Up section $error",
                                    null
                                )
                            )
                        )
                    )
                }
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getLatestSection(
        libraries: List<Library>,
        retrieveFromCache: Boolean
    ): Flow<Resource<List<HomeSectionRow>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<HomeSectionRow>>> {
            emit(Resource.loading())
            try {
                val rows = mutableListOf<HomeSectionRow>()
                libraries.forEachIndexed { libraryIndex, library ->
                    val cachedRow = homeRowCache[library.id.toString()]
                    if (retrieveFromCache && cachedRow != null) {
                        rows.add(cachedRow)
                    } else {
                        val result by userLibraryApi.getLatestMedia(
                            userId = userId,
                            parentId = library.uuid,
                            limit = 16,
                            fields = listOf(
                                ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                                ItemFields.BASIC_SYNC_INFO,
                                ItemFields.PATH
                            ),
                            imageTypeLimit = 1,
                            enableImageTypes = listOf(
                                ImageType.PRIMARY,
                                ImageType.BACKDROP,
                                ImageType.THUMB
                            )
                        )
                        if (result.isNotEmpty()) {
                            val cards = mutableListOf<HomeSectionCard>()
                            // TODO: Use a string repository to get the "Latest x" string
                            result.forEachIndexed { resultIndex, item ->
                                val itemId = when (item.type) {
                                    ItemType.EPISODE.type -> item.seriesId ?: item.id
                                    else -> item.id
                                }
                                val imageType = when (item.type) {
                                    ItemType.TV_CHANNEL.type -> ImageType.BACKDROP
                                    else -> ImageType.PRIMARY
                                }
                                val title = when (item.type) {
                                    ItemType.EPISODE.type -> item.seriesName
                                    else -> item.name
                                }
                                val homeCardType = when (item.type) {
                                    ItemType.TV_CHANNEL.type -> HomeCardType.BACKDROP
                                    else -> HomeCardType.POSTER
                                }
                                val fillWidth = when (item.type) {
                                    ItemType.TV_CHANNEL.type -> 335
                                    else -> 223
                                }
                                val fillHeight = when (item.type) {
                                    ItemType.TV_CHANNEL.type -> 223
                                    else -> 335
                                }
                                val imageUrl = imageApi.getItemImageUrl(
                                    itemId = itemId,
                                    imageType = imageType,
                                    fillWidth = fillWidth,
                                    fillHeight = fillHeight
                                )
                                val itemType = when (item.type) {
                                    ItemType.MOVIE.type -> ItemType.MOVIE
                                    ItemType.SERIES.type -> ItemType.SERIES
                                    else -> ItemType.MOVIE
                                }
                                val map = if (item.imageBlurHashes.containsKey(ImageType.PRIMARY)) item.imageBlurHashes[ImageType.PRIMARY] else null
                                val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                                cards.add(
                                    HomeSectionCard(
                                        id = resultIndex,
                                        imageUrl = imageUrl,
                                        title = title,
                                        subtitle = null,
                                        uuid = itemId,
                                        homeCardType = homeCardType,
                                        homeCardAction = HomeCardAction.DETAILS,
                                        itemType = itemType,
                                        blurHash = blurHash
                                    )
                                )
                            }
                            val row = HomeSectionRow(
                                id = HomeSectionType.LATEST_MEDIA.ordinal + libraryIndex,
                                title = "Latest " + library.title,
                                cards = cards
                            )
                            homeRowCache[library.id.toString()] = row
                            rows.add(row)
                        }
                    }
                }
                // TODO: Use a repo to get My Media string
                emit(Resource.success(rows))
            } catch (e: Exception) {
                // TODO: Need to catch httpException and pass along correct error message
                val error = e.message
                emit(
                    Resource.error(
                        listOf(
                            Error(
                                1,
                                1,
                                "Could not load Latest Items section $error",
                                null
                            )
                        )
                    )
                )
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getHomeSections(): Flow<Resource<List<HomeSectionType>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<HomeSectionType>>> {
            emit(Resource.loading())
            val response = mutableListOf<HomeSectionType>()
            try {
                // Get the web display preferences (i.e. set client to emby)
                val result by displayPreferencesApi.getDisplayPreferences(userId = userId, displayPreferencesId = "usersettings", client = "emby")
                // TODO: Figure out the API call the returns the actual list of home sections and add them to the response in the correct order
                for (i in 0..6) {
                    val key = "homesection$i"
                    val pref = result.customPrefs?.get(key)
                    pref?.let {type ->
                        val item = when (type) {
                            HomeSectionType.MY_MEDIA.type -> HomeSectionType.MY_MEDIA
                            HomeSectionType.CONTINUE_WATCHING.type -> HomeSectionType.CONTINUE_WATCHING
                            HomeSectionType.NEXT_UP.type -> HomeSectionType.NEXT_UP
                            HomeSectionType.LATEST_MEDIA.type -> HomeSectionType.LATEST_MEDIA
                            else -> null
                        }
                        item?.let {
                            if (!response.contains(it)) {
                                response.add(it)
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }

            // If any section was not set in a custom order then set it
            if (!response.contains(HomeSectionType.MY_MEDIA)) {
                response.add(HomeSectionType.MY_MEDIA)
            }
            if (!response.contains(HomeSectionType.CONTINUE_WATCHING)) {
                response.add(HomeSectionType.CONTINUE_WATCHING)
            }
            if (!response.contains(HomeSectionType.NEXT_UP)) {
                response.add(HomeSectionType.NEXT_UP)
            }
            if (!response.contains(HomeSectionType.LATEST_MEDIA)) {
                response.add(HomeSectionType.LATEST_MEDIA)
            }

            emit(Resource.success(response))
        }.flowOn(networkDispatcher)
    }

    override suspend fun getRecentItems(): Flow<Resource<List<CachedRecentItem>>> {
        val userId = currentUserRepository.getCurrentUserId() ?: throw IllegalArgumentException("UserId cannot be null")
        return flow<Resource<List<CachedRecentItem>>> {
            emit(Resource.loading())
            try {
                val result by userLibraryApi.getLatestMedia(
                    userId = userId,
                    limit = RECENT_ITEMS_MAX_COUNT,
                    fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.OVERVIEW),
                    imageTypeLimit = 1,
                    enableImageTypes = listOf(ImageType.LOGO, ImageType.BACKDROP)
                )
                val response = mutableListOf<CachedRecentItem>()
                result.forEachIndexed { index, item ->
                    val map = if (item.imageBlurHashes.containsKey(ImageType.BACKDROP)) item.imageBlurHashes[ImageType.BACKDROP] else null
                    val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                    response.add(CachedRecentItem(id = index,
                        uuid = item.id,
                        seriesId = item.seriesId,
                        blurHash = blurHash,
                        containsBackdropImages = item.backdropImageTags?.isNotEmpty() == true || item.parentBackdropImageTags?.isNotEmpty() == true,
                        type = item.type,
                        name = item.name,
                        seriesName = item.seriesName
                    ))
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                val error = e.message
                emit(Resource.error(
                    listOf(Error(null, 1, "Could not load Recent Items section $error",null))
                    )
                )
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getMovieDetails(movieId: UUID): Flow<Resource<MovieDetails>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<MovieDetails>> {
            emit(Resource.loading())
            try {
                val result by userLibraryApi.getItem(userId = userId, itemId = movieId)
                val people = result.people?.map {
                    Person(
                        id = it.id,
                        name = it.name,
                        type = it.type,
                        primaryImageTag = it.primaryImageTag,
                        role = it.role
                    )
                }
                val backdropUrl = imageApi.getItemImageUrl(
                    itemId = movieId,
                    imageType = ImageType.BACKDROP,
                    maxWidth = 1920,
                    maxHeight = 1080
                )
                val posterUrl = imageApi.getItemImageUrl(
                    itemId = movieId,
                    imageType = ImageType.PRIMARY,
                    maxWidth = 1000,
                    maxHeight = 1500
                )

                val backdropMap = if (result.imageBlurHashes.containsKey(ImageType.BACKDROP)) result.imageBlurHashes[ImageType.BACKDROP] else null
                val backdropBlurHash = if (backdropMap?.isNotEmpty() == true) backdropMap.values.first() else null

                val posterMap = if (result.imageBlurHashes.containsKey(ImageType.PRIMARY)) result.imageBlurHashes[ImageType.PRIMARY] else null
                val posterBlurHash = if (posterMap?.isNotEmpty() == true) posterMap.values.first() else null

                val response = MovieDetails(
                    id = movieId,
                    name = result.name,
                    year = result.premiereDate?.year.toString(),
                    communityRating = result.communityRating,
                    criticRating = result.criticRating,
                    container = result.container,
                    externalUrls = emptyList(),
                    backdropUrl = backdropUrl,
                    genres = result.genreItems?.mapIndexed { index, item -> Genre(index, item.name, item.id) },
                    posterUrl = posterUrl,
                    officialRating = result.officialRating,
                    overview = result.overview,
                    actors = people?.filter { it.type != null && it.type.equals(PersonType.ACTOR) },
                    directors = people?.filter { it.type != null && it.type.equals(PersonType.DIRECTOR) },
                    runTimeTicks = result.runTimeTicks,
                    tagLines = result.taglines,
                    backdropBlurHash = backdropBlurHash,
                    posterBlurHash = posterBlurHash
                )
                emit(Resource.success(response))
            } catch (e: Exception) {
                val error = e.message
                emit(
                    Resource.error(
                        listOf(
                            Error(
                                null,
                                1,
                                "Could not load Home section $error",
                                null
                            )
                        )
                    )
                )
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getSeriesDetails(seriesId: UUID): Flow<Resource<SeriesDetails>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<SeriesDetails>> {
            emit(Resource.loading())
            try {
                val result by userLibraryApi.getItem(userId = userId, itemId = seriesId)
                val people = result.people?.map {
                    Person(
                        id = it.id,
                        name = it.name,
                        type = it.type,
                        primaryImageTag = it.primaryImageTag,
                        role = it.role
                    )
                }
                val backdropUrl = imageApi.getItemImageUrl(
                    itemId = seriesId,
                    imageType = ImageType.BACKDROP,
                    maxWidth = 960,
                    maxHeight = 540
                )
                val posterUrl = imageApi.getItemImageUrl(
                    itemId = seriesId,
                    imageType = ImageType.PRIMARY,
                    maxWidth = 500,
                    maxHeight = 750
                )
                val backdropMap = if (result.imageBlurHashes.containsKey(ImageType.BACKDROP)) result.imageBlurHashes[ImageType.BACKDROP] else null
                val backdropBlurHash = if (backdropMap?.isNotEmpty() == true) backdropMap.values.first() else null

                val posterMap = if (result.imageBlurHashes.containsKey(ImageType.PRIMARY)) result.imageBlurHashes[ImageType.PRIMARY] else null
                val posterBlurHash = if (posterMap?.isNotEmpty() == true) posterMap.values.first() else null

                val response = SeriesDetails(
                    id = seriesId,
                    name = result.name,
                    year = result.premiereDate?.year.toString(),
                    communityRating = result.communityRating,
                    criticRating = result.criticRating,
                    container = result.container,
                    externalUrls = emptyList(),
                    backdropUrl = backdropUrl,
                    genres = result.genreItems?.mapIndexed { index, item -> Genre(index, item.name, item.id) },
                    posterUrl = posterUrl,
                    officialRating = result.officialRating,
                    overview = result.overview,
                    actors = people?.filter { it.type != null && it.type.equals(PersonType.ACTOR) },
                    directors = people?.filter { it.type != null && it.type.equals(PersonType.DIRECTOR) },
                    runTimeTicks = result.runTimeTicks,
                    tagLines = result.taglines,
                    seasons = null,
                    nextEpisode = null,
                    backdropBlurHash = backdropBlurHash,
                    posterBlurHash = posterBlurHash
                )
                emit(Resource.success(response))
            } catch (e: Exception) {
                val error = e.message
                emit(
                    Resource.error(
                        listOf(
                            Error(
                                null,
                                1,
                                "Could not load Home section $error",
                                null
                            )
                        )
                    )
                )
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getSeasons(seriesId: UUID): Flow<Resource<List<Season>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<Season>>> {
            emit(Resource.loading())
            try {
                val result by tvShowsApi.getSeasons(userId = userId,
                    seriesId = seriesId,
                    fields = listOf(ItemFields.ITEM_COUNTS, ItemFields.MEDIA_SOURCE_COUNT))
                val response = mutableListOf<Season>()
                val posterUrl = imageApi.getItemImageUrl(
                    itemId = seriesId,
                    imageType = ImageType.PRIMARY,
                    maxWidth = 1000,
                    maxHeight = 1500
                )
                result.items?.forEachIndexed { index, item ->
                    val map = if (item.imageBlurHashes.containsKey(ImageType.PRIMARY)) item.imageBlurHashes[ImageType.PRIMARY] else null
                    val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                    val season = Season(id = index,
                        seasonId = item.id,
                        name = item.name,
                        seriesId = seriesId,
                        imageUrl = posterUrl,
                        unPlayedItemCount = item.userData?.unplayedItemCount ?: 0,
                        blurHash = blurHash
                    )
                    response.add(season)
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                val error = e.message
                emit(
                    Resource.error(
                        listOf(
                            Error(
                                null,
                                1,
                                "Could not load Home section $error",
                                null
                            )
                        )
                    )
                )
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getLibraryItems(pageNumber: Int, pageSize: Int, library: Library, genre: Genre): List<HomeSectionCard> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UserId cannot be null")

        val genreId = if (genre.id == 0) null else genre.genreId
        val genres = if (genreId != null) listOf(genreId) else null
        val result by itemsApi.getItems(
            userId = userId,
            sortBy = listOf("SortName,ProductionYear"),
            sortOrder = listOf(SortOrder.ASCENDING),
            includeItemTypes = listOf(library.type.type),
            recursive = true,
            fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.MEDIA_SOURCE_COUNT, ItemFields.BASIC_SYNC_INFO),
            genreIds = genres,
            imageTypeLimit = 1,
            enableImageTypes = listOf(ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB),
            startIndex = pageNumber * pageSize,
            limit = pageSize,
            parentId = library.uuid,
        )

        val response = mutableListOf<HomeSectionCard>()
        result.items?.forEachIndexed { index, item ->
            val imageUrl = imageApi.getItemImageUrl(
                itemId = item.id,
                imageType = ImageType.PRIMARY
            )
            val map = if (item.imageBlurHashes.containsKey(ImageType.PRIMARY)) item.imageBlurHashes[ImageType.PRIMARY] else null
            val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
            response.add(
                HomeSectionCard(
                    id = index,
                    imageUrl = imageUrl,
                    title = item.name,
                    subtitle = null,
                    homeCardType = HomeCardType.POSTER,
                    uuid = item.id,
                    homeCardAction = HomeCardAction.DETAILS,
                    itemType = library.type,
                    blurHash = blurHash
                )
            )
        }

        return response
    }

    override suspend fun getGenres(libraryId: UUID, itemType: ItemType): Flow<Resource<List<Genre>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<Genre>>> {
            emit(Resource.loading())
            val response = mutableListOf<Genre>()
            val allGenre = Genre(GENRE_ALL_ID, "All", null)
            response.add(allGenre)

            try {
                val result by genresApi.getGenres(userId = userId,
                    parentId = libraryId,
                    includeItemTypes = listOf(itemType.type),
                    enableTotalRecordCount = false)
                result.items?.forEachIndexed { index, item ->
                    val genre = Genre(id = index + 1, name = item.name, genreId = item.id)
                    response.add(genre)
                }
                emit(Resource.success(response))
            } catch (e: Exception) {
                // Return all genres even if there is an error
                emit(Resource.success(response))
            }
        }.flowOn(networkDispatcher)
    }

    override suspend fun getEpisodes(seriesId: UUID, seasonId: UUID): Flow<Resource<List<Episode>>> {
        val userId = currentUserRepository.getCurrentUserId()
            ?: throw IllegalArgumentException("UseId cannot be null")
        return flow<Resource<List<Episode>>> {
            emit(Resource.loading())
            try {
                val response = mutableListOf<Episode>()
                val result by tvShowsApi.getEpisodes(seriesId = seriesId,
                    userId = userId,
                    seasonId = seasonId,
                    fields = listOf(ItemFields.ITEM_COUNTS, ItemFields.OVERVIEW))
                result.items?.forEachIndexed { index, item ->
                    val imageUrl = imageApi.getItemImageUrl(
                        itemId = item.id,
                        imageType = ImageType.PRIMARY,
                        fillWidth = 500,
                        fillHeight = 500
                    )
                    val map = if (item.imageBlurHashes.containsKey(ImageType.BACKDROP)) item.imageBlurHashes[ImageType.BACKDROP] else null
                    val blurHash = if (map?.isNotEmpty() == true) map.values.first() else null
                    val episode = Episode(id = index + 1,
                        episodeId = item.id,
                        name = item.name,
                        description = item.overview,
                        runTime = item.runTimeTicks?.toString(), // TODO: format as time string (i.e. 43 mins)
                        communityRating = item.communityRating,
                        imageUrl = imageUrl,
                        blurHash = blurHash)
                    response.add(episode)
                }
                emit(Resource.success(response))
            } catch (e: Exception) {

            }
        }.flowOn(networkDispatcher)
    }

    override fun clearCache() {
        homeRowCache.clear()
    }
}
