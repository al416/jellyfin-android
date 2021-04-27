package org.jellyfin.client.android.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.jellyfin.client.android.databinding.HomeDetailsSectionRowBinding
import org.jellyfin.client.android.domain.models.display_model.HomeSectionRow


class HomeRowRecyclerViewAdapter(private val context: Context) : ListAdapter<HomeSectionRow, HomeRowRecyclerViewAdapter.RowViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<HomeSectionRow>() {
        override fun areItemsTheSame(oldItem: HomeSectionRow, newItem: HomeSectionRow): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeSectionRow, newItem: HomeSectionRow): Boolean {
            return oldItem == newItem
        }
    }

    class RowViewHolder(val binding: HomeDetailsSectionRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HomeDetailsSectionRowBinding.inflate(layoutInflater)
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.binding.cardsRecyclerView.layoutManager = layoutManager

        val row = getItem(position)
        holder.binding.row = row
        holder.binding.cardsRecyclerView.adapter = HomeCardRecyclerViewAdapter(row.cards)

        holder.binding.executePendingBindings()
    }
}
