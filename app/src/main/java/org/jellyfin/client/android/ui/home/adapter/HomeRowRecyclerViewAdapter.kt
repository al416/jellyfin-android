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
import org.jellyfin.client.android.domain.models.display_model.HomeContents


class HomeRowRecyclerViewAdapter(private val contents: HomeContents,
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
        return contents.sections.size
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.cardRecyclerView.layoutManager = layoutManager

        val rowType = contents.sections[position]
        val filteredList = contents.cards.filter { it.rowId == rowType.id}

        holder.rowTitle.text = rowType.title
        holder.cardRecyclerView.adapter = HomeCardRecyclerViewAdapter(filteredList)
    }
}
