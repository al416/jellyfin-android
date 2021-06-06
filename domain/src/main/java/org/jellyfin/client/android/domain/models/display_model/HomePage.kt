package org.jellyfin.client.android.domain.models.display_model

data class HomePage(val recentItems: List<RecentItem>,
                    val rows: List<HomeSectionRow>)
