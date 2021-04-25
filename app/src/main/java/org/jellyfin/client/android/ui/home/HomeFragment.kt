package org.jellyfin.client.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R
import org.jellyfin.client.android.display_model.HomeSectionRow
import org.jellyfin.client.android.ui.home.adapter.HomeRowRecyclerViewAdapter


class HomeFragment : DaggerFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var homeRowAdapter: RecyclerView.Adapter<HomeRowRecyclerViewAdapter.RowViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeRowAdapter = HomeRowRecyclerViewAdapter(generateTestRows(), requireActivity())

        recyclerView = ViewCompat.requireViewById(view, R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = homeRowAdapter
        homeRowAdapter.notifyDataSetChanged()
    }

    /**
     * This fun will be removed once we have real data
     */
    private fun generateTestRows(): ArrayList<HomeSectionRow> {
        val rows = ArrayList<HomeSectionRow>()
        rows.add(HomeSectionRow(0, "My Media"))
        rows.add(HomeSectionRow(1, "Continue Watching"))
        rows.add(HomeSectionRow(2, "Next Up"))
        rows.add(HomeSectionRow(3, "Latest Movies"))
        rows.add(HomeSectionRow(4, "Latest TV Shows"))
        return rows
    }

}