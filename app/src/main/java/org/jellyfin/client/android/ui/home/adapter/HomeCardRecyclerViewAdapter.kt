package org.jellyfin.client.android.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard

class HomeCardRecyclerViewAdapter(var cardList: List<HomeSectionCard>) :
    ListAdapter<HomeSectionCard, HomeCardRecyclerViewAdapter.CardViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<HomeSectionCard>() {
        override fun areItemsTheSame(oldItem: HomeSectionCard, newItem: HomeSectionCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeSectionCard, newItem: HomeSectionCard): Boolean {
            return oldItem == newItem
        }

    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardBackgroundImage: ImageView = ViewCompat.requireViewById(itemView, R.id.card_background_image)
        var title: TextView = ViewCompat.requireViewById(itemView, R.id.card_title)
        var subtitle: TextView = ViewCompat.requireViewById(itemView, R.id.card_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layout = if (viewType == HomeCardType.POSTER.ordinal) R.layout.home_details_section_card_poster else R.layout.home_details_section_card_backdrop
        val view: View = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        // TODO: Add the correct placeholder and error images
        holder.cardBackgroundImage.load(card.imageUrl) {
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_background)
        }
        holder.title.text = card.title
        holder.subtitle.text = card.subtitle
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun getItemViewType(position: Int): Int {
        return cardList[position].homeCardType.ordinal
    }
}
