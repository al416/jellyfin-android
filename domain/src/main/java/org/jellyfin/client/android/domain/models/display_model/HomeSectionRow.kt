package org.jellyfin.client.android.domain.models.display_model

data class HomeSectionRow(
    var id: Int,
    val title: String?
)

enum class HomeSectionType {
    MY_MEDIA,
    CONTINUE_WATCHING,
    NEXT_UP,
    LATEST_MEDIA
}