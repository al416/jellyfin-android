package org.jellyfin.client.android.domain.models

import java.util.*

data class LibraryDto(val id: Int, val uuid: UUID, val title: String?, val type: String)