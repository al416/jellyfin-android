package org.jellyfin.client.android.domain.models.display_model

import java.util.*

data class HomeSectionCard(
    val id: Int,
    val backgroundImage: Int,
    val title: String?,
    val subtitle: String? = null,
    val homeCardType: HomeCardType,
    val uuid: UUID,
    var rowId: Int = 0
    //val clickListener: View.OnClickListener? = null
)

enum class HomeCardType {
    SECTION,    // clicking this card will take the user to Movies/TV Shows list page
    PLAYABLE_ITEM,  // clicking this card will play the item (i.e. cards under Continue Watching/Next Up)
    DETAILS // clicking this card will take user to Movie/TV Shows Details page (i.e. under Latest Movies)
}