package org.jellyfin.client.android.domain.models.display_model

import org.jellyfin.client.android.domain.models.Status

data class HomePageItem(val id: Int, val type: String, val row: HomeSectionRow?, val status: Status)