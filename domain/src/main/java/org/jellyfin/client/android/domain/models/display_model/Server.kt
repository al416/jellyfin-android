package org.jellyfin.client.android.domain.models.display_model

data class Server(val id: Int,
                  val name: String,
                  val url: String,
                  val displayOrder: Int)