package org.jellyfin.client.android.domain.models

import java.io.Serializable
import java.util.*

data class Library(val id: Int,
                   val uuid: UUID,
                   val title: String?,
                   val type: String) : Serializable
