package org.jellyfin.client.android.domain.models.display_model

import java.io.Serializable
import java.util.*

data class MovieDetails(
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
    val runTimeTicks: Long?,
    val tagLines: List<String>?
)

data class ExternalUrl(val name: String, val url: String)

data class Genre(val id: Int,
                 val name: String?,
                 val genreId: UUID?) : Serializable

data class Person(
    val name: String?,
    val id: String?,
    val primaryImageTag: String?,
    val role: String?,
    val type: String?
)