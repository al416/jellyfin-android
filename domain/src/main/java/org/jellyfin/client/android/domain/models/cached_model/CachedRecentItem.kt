package org.jellyfin.client.android.domain.models.cached_model

import java.util.*

data class CachedRecentItem(
    var id: Int,
    val uuid: UUID,
    val seriesId: UUID? = null,   // only applicable to episodes and series
    val blurHash: String?,
    val containsBackdropImages: Boolean,
    val type: String?,
    val name: String?,
    val seriesName: String?
)
