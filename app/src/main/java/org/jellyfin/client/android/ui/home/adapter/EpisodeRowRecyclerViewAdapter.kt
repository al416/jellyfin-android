package org.jellyfin.client.android.ui.home.adapter


import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_WIDTH
import org.jellyfin.client.android.domain.models.display_model.Episode
import org.jellyfin.client.android.ui.shared.BlurHashDecoder

class EpisodeRowRecyclerViewAdapter(private val context: Context) :
    ListAdapter<Episode, EpisodeRowRecyclerViewAdapter.EpisodeViewHolder>(Companion) {

    var onCardClick: ((Episode) -> Unit)? = null

    companion object: DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: CardView = ViewCompat.requireViewById(itemView, R.id.card)
        var cardBackgroundImage: ImageView = ViewCompat.requireViewById(itemView, R.id.card_background_image)
        var title: TextView = ViewCompat.requireViewById(itemView, R.id.tvEpisodeTitle)
        var description: TextView = ViewCompat.requireViewById(itemView, R.id.tvEpisodeDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.episode_row, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)
        // TODO: Add the correct placeholder and error images
        val bitmap = BlurHashDecoder.decode(episode.blurHash, BLUR_HASH_BACKDROP_WIDTH, BLUR_HASH_BACKDROP_HEIGHT)
        val drawable = BitmapDrawable(context.resources, bitmap)
        holder.cardBackgroundImage.load(episode.imageUrl) {
            placeholder(drawable)
            error(drawable)
        }
        holder.cardView.setOnClickListener {
            onCardClick?.invoke(episode)
        }
        holder.title.text = episode.name
        holder.description.text = episode.description
    }
}
