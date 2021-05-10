package org.jellyfin.client.android.domain.models.display_model

import java.io.Serializable

data class Server(val id: Int,
                  val name: String,
                  val url: String,
                  val displayOrder: Int) : Serializable

data class ServerList(val servers: List<Server>) : Serializable