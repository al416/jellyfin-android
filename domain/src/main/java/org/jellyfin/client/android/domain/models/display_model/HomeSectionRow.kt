package org.jellyfin.client.android.domain.models.display_model

import org.jellyfin.client.android.domain.models.Status

data class HomeSectionRow(
    var id: Int,
    val title: String?,
    val cards: List<HomeSectionCard>,
    val status: Status
)

enum class HomeSectionType(val type: String) {
    MY_MEDIA("smalllibrarytiles"),
    CONTINUE_WATCHING("resume"),
    NEXT_UP("nextup"),
    LATEST_MEDIA("latestmedia")
}