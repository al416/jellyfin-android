package org.jellyfin.client.android.domain.models.display_model

import java.util.*

data class SeriesDetails(
    val id: UUID,
    val name: String?,
    val year: String?,
    val communityRating: Float?,
    val criticRating: Float?,
    val container: String?,
    val externalUrls: List<ExternalUrl>?,
    val backdropUrl: String,
    val genres: List<Genre>?,
    val posterUrl: String,
    val officialRating: String?,
    val overview: String?,
    val actors: List<Person>?,
    val directors: List<Person>?,
    val tagLines: List<String>?,
    var seasons: HomeSectionRow? = null,
    var nextEpisode: Episode?,
    val backdropBlurHash: String?,
    val posterBlurHash: String?,
    var similarItems: HomeSectionRow? = null
)

data class Season(val id: Int,
                  val seasonId: UUID,
                  val name: String?,
                  val seriesId: UUID,
                  val imageUrl: String,
                  val unPlayedItemCount: Int,
                  var blurHash: String?)

data class Episode(val id: Int,
                   val episodeId: UUID,
                   val name: String?,
                   val description: String?,
                   val runTime: String?,
                   val communityRating: Float?,
                   val imageUrl: String,
                   var blurHash: String?,
                   var isFavorite: Boolean = false,
                   var isWatched: Boolean = false)
