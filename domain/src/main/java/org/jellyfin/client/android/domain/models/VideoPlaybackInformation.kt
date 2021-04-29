package org.jellyfin.client.android.domain.models

data class VideoPlaybackInformation(val url: String? = null, val videoPlayType: VideoPlayType)

enum class VideoPlayType{
    DIRECT_PLAY,
    TRANSCODING,
    LIVE_TV
}