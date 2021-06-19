package org.jellyfin.client.android.domain.models.cached_model

import java.util.*

// TODO: The UUIDs might need to be converted to strings so they can be inserted to the database
data class CachedBaseItem(
    val id: UUID,
    val seriesId: UUID? = null,   // only applicable to episodes and series
    val blurHash: String?,
    val containsBackdropImages: Boolean,
    val type: String?,
    val name: String?,
    val seriesName: String?
)
