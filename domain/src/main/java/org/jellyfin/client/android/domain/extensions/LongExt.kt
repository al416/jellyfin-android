package org.jellyfin.client.android.domain.extensions

fun Long.formatTime(): String {
    val seconds = String.format("%02d", (this / 1000) % 60)
    val minutes = String.format("%02d", (this / (1000 * 60) % 60))
    val hours = String.format("%02d", (this / (1000 * 60 * 60) % 24))
    return "$hours:$minutes:$seconds"
}
