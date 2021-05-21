package org.jellyfin.client.android.ui.home.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PrefetchLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean, private val extraSpace: Int) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    // Load additional items in the Recyclerview so images load faster when the user scrolls in the recyclerview
    override fun getExtraLayoutSpace(state: RecyclerView.State?) = extraSpace

}