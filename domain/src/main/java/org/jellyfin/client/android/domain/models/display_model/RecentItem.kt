package org.jellyfin.client.android.domain.models.display_model

import org.jellyfin.client.android.domain.constants.ItemType
import java.util.*

data class RecentItem(
    var id: Int,
    val imageUrl: String,
    val title: String?,
    val subtitle: String? = null,
    val uuid: UUID,
    val seriesUUID: UUID? = null,   // only applicable to episodes and series
    val itemType: ItemType,
    val blurHash: String?
)
