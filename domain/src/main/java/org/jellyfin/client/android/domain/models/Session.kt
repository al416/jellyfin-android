package org.jellyfin.client.android.domain.models


data class Session(val sessionId: Int,
                   val serverId: Int,
                   val serverUrl: String,
                   val userName: String,
                   val userUUID: String,
                   val apiKey: String)
