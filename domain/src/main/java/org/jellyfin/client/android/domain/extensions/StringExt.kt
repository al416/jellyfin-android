package org.jellyfin.client.android.domain.extensions

import org.jellyfin.client.android.domain.constants.SubtitleLanguage

fun String.getSubtitleDescription(): String {
    val subtitles = enumValues<SubtitleLanguage>()
    val subtitle = subtitles.firstOrNull { it.code == this }
    return subtitle?.description ?: SubtitleLanguage.ENGLISH.description
}
