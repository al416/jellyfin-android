package org.jellyfin.client.android.ui.shared

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.jellyfin.client.android.R

class RowWithChevronView(context: Context)
    : LinearLayout(context) {

    private val tvTitle: TextView
    private val root: ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.row_with_chevron, this, true)
        tvTitle = findViewById(R.id.tvTitle)
        root = findViewById(R.id.root)
    }

    fun setText(title: String?) {
        tvTitle.text = title
    }

    override fun setOnClickListener(l: OnClickListener?) {
        root.setOnClickListener(l)
    }
}
