package org.jellyfin.client.android.ui.home.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
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
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_WIDTH
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_POSTER_WIDTH
import org.jellyfin.client.android.domain.models.display_model.HomeCardType
import org.jellyfin.client.android.domain.models.display_model.HomeSectionCard
import org.jellyfin.client.android.ui.shared.BlurHashDecoder

class HomeCardRecyclerViewAdapter(private val context: Context) :
    ListAdapter<HomeSectionCard, HomeCardRecyclerViewAdapter.CardViewHolder>(Companion) {

    var onCardClick: ((HomeSectionCard) -> Unit)? = null

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
        val card = getItem(position)
        // TODO: Add the correct placeholder and error images
        val width = if (card.homeCardType == HomeCardType.POSTER) BLUR_HASH_POSTER_WIDTH else BLUR_HASH_BACKDROP_WIDTH
        val height = if (card.homeCardType == HomeCardType.POSTER) BLUR_HASH_POSTER_HEIGHT else BLUR_HASH_BACKDROP_HEIGHT
        val bitmap = BlurHashDecoder.decode(card.blurHash, width, height)
        val drawable = BitmapDrawable(context.resources, bitmap)
        holder.cardBackgroundImage.load(card.imageUrl) {
            placeholder(drawable)
            error(drawable)
        }
        holder.itemView.setOnClickListener {
            onCardClick?.invoke(card)
        }
        holder.title.text = card.title
        holder.title.isSelected = true
        holder.subtitle.text = card.subtitle
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).homeCardType.ordinal
    }
}
