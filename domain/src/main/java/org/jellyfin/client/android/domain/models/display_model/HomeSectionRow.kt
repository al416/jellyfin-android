package org.jellyfin.client.android.domain.models.display_model

data class HomeSectionRow(
    var id: Int,
    val title: String?,
    val cards: List<HomeSectionCard>
)

enum class HomeSectionType(val type: String) {
    MY_MEDIA("smalllibrarytiles"),
    CONTINUE_WATCHING("resume"),
    NEXT_UP("nextup"),
    LATEST_MEDIA("latestmedia")
}