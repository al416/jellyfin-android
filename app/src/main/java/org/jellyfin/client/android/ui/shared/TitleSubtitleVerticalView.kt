package org.jellyfin.client.android.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.jellyfin.client.android.R

class TitleSubtitleVerticalView(context: Context, attrs: AttributeSet)
    : LinearLayout(context, attrs) {

    private val tvTitle: TextView
    private val tvSubtitle: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.title_subtitle_vertical_view, this, true)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
    }

    fun setTextAndVisibility(title: String?, subtitle: String?) {
        if (subtitle.isNullOrBlank()) {
            this.visibility = View.GONE
        } else {
            tvTitle.text = title
            tvSubtitle.text = subtitle
        }
    }
}
