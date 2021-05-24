package org.jellyfin.client.tv.ui.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.tv.databinding.ServerRecyclerItemBinding
import java.util.*

/*
    This adapter is different from the mobile version of the adapter. This adapter uses single click for Edit/Delete action popup and
    long press for Reorder. The mobile version relies on touch for those actions.
 */
class TvServerRecyclerViewAdapter() :
    ListAdapter<Server, TvServerRecyclerViewAdapter.ServerViewHolder>(Companion) {

    var onListChanged: ((List<Server>) -> Unit)? = null

    var onItemClicked: ((Server) -> Unit)? = null

    companion object: DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem == newItem
        }
    }

    class ServerViewHolder(val binding: ServerRecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ServerRecyclerItemBinding.inflate(layoutInflater)
        return ServerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = getItem(position)
        holder.binding.server = server
        holder.binding.item.setOnClickListener {
            onItemClicked?.invoke(server)
        }
        holder.binding.executePendingBindings()
    }

    fun onItemMove(oldPosition: Int, newPosition: Int): Boolean {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        Collections.swap(orderedList, oldPosition, newPosition)
        submitList(orderedList)
        return true
    }

    fun onItemDismiss(position: Int) {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        orderedList.removeAt(position)
        submitList(orderedList)
    }

    fun onTouchActionComplete() {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        submitList(orderedList)
        onListChanged?.invoke(orderedList)
    }
}
