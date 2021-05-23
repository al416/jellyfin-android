package org.jellyfin.client.android.domain.constants

object CollectionType {
    const val MOVIES = "movies"
    const val TV_SHOWS = "tvshows"
}

enum class ItemType(val type: String) {
    MOVIE("Movie"),
    EPISODE("Episode"),
    TV_CHANNEL("TvChannel"),
    SERIES("Series"),
    SEASON("Season")
}

object PersonType {
    const val ACTOR = "Actor"
    const val DIRECTOR = "Director"
}