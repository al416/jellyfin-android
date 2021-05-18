package org.jellyfin.client.android.ui.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.jellyfin.client.android.databinding.ServerRecyclerItemBinding
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.ui.login.add_server.ServerItemTouchListener
import java.util.*

class ServerRecyclerViewAdapter() :
    ListAdapter<Server, ServerRecyclerViewAdapter.ServerViewHolder>(Companion), ServerItemTouchListener {

    var onListChanged: ((List<Server>) -> Unit)? = null

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
        holder.binding.executePendingBindings()
    }

    override fun onItemMove(oldPosition: Int, newPosition: Int): Boolean {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        Collections.swap(orderedList, oldPosition, newPosition)
        submitList(orderedList)
        return true
    }

    override fun onItemDismiss(position: Int) {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        orderedList.removeAt(position)
        submitList(orderedList)
    }

    override fun onTouchActionComplete() {
        val orderedList = mutableListOf<Server>()
        orderedList.addAll(currentList)
        submitList(orderedList)
        onListChanged?.invoke(orderedList)
    }
}
