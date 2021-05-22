package org.jellyfin.client.android.domain.models.display_model

import java.util.*

data class HomeSectionCard(
    var id: Int,
    val imageUrl: String,
    val title: String?,
    val subtitle: String? = null,
    val homeCardType: HomeCardType,
    val uuid: UUID,
    var rowId: Int = 0,
    val homeCardAction: HomeCardAction,
    val itemType: String
    //val clickListener: View.OnClickListener? = null
)

enum class HomeCardType {
    POSTER,
    BACKDROP
}

enum class HomeCardAction {
    NO_ACTION,
    DETAILS,
    PLAY
}