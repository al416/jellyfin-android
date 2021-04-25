package org.jellyfin.client.android.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jellyfin.client.android.R
import org.jellyfin.client.android.display_model.HomeCardType
import org.jellyfin.client.android.display_model.HomeSectionCard
import org.jellyfin.client.android.display_model.HomeSectionRow


class HomeRowRecyclerViewAdapter(private val rowList: ArrayList<HomeSectionRow>,
                                 private val context: Context) :
    RecyclerView.Adapter<HomeRowRecyclerViewAdapter.RowViewHolder>() {

    class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rowTitle: TextView = ViewCompat.requireViewById(itemView, R.id.row_title)
        var cardRecyclerView: RecyclerView = ViewCompat.requireViewById(itemView, R.id.cards_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.home_details_section_row, parent, false)
        return RowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rowList.size
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val row = rowList[position]
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.cardRecyclerView.layoutManager = layoutManager

        val testData = generateTestData()
        val filteredList = testData.filter { it.rowId == position}

        holder.rowTitle.text = row.title
        holder.cardRecyclerView.adapter = HomeCardRecyclerViewAdapter(filteredList)
    }

    /**
     * This fun will be removed once we have real data
     */
    private fun generateTestData(): ArrayList<HomeSectionCard> {
        val cards = ArrayList<HomeSectionCard>()
        cards.add(HomeSectionCard(id = 0, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 0, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 1, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media 1", subtitle = "1990", rowId = 0, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 2, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media 2", subtitle = "1980", rowId = 0, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 3, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 0, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 4, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 5, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 6, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 7, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 8, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 9, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 1, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 10, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 2, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 11, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 2, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 12, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 3, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 13, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 3, homeCardType = HomeCardType.SECTION))
        cards.add(HomeSectionCard(id = 14, backgroundImage = R.drawable.ic_launcher_background, title = "Test Media", subtitle = null, rowId = 4, homeCardType = HomeCardType.SECTION))
        return cards
    }
}
